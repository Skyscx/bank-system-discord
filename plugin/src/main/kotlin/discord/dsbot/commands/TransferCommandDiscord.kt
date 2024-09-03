package discord.dsbot.commands

import App.Companion.configPlugin
import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class TransferCommandDiscord(config: FileConfiguration, discordBot: DiscordBot) : ListenerAdapter() {
    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(discordBot.getJDA(), config)
    private val allowedChannelId = config.getLong("allowed-channel-id-for-bank-commands")

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "transfer") return
        val channel = event.channel as MessageChannel
        val user = event.user
        if (channel.idLong != allowedChannelId) {
            event.reply("Эту команду можно использовать только в <#$allowedChannelId> канале.").setEphemeral(true).queue()
            return
        }

        val targetMember = event.getOption("user")?.asMember ?: return
        val amount = event.getOption("amount")?.asLong ?: return

        // Проверяем, что сумма перевода положительная
        if (amount <= 0) {
            event.reply("Сумма перевода должна быть положительной.").setEphemeral(true).queue()
            return
        }

        // Операция перевода
        val discordIDSender = user.id
        val discordIDTarget = targetMember.id

        val uuidSenderFuture = userDB.getUUIDbyDiscordID(discordIDSender)
        val uuidTargetFuture = userDB.getUUIDbyDiscordID(discordIDTarget)

        uuidSenderFuture.thenAccept { uuidSender ->
            if (uuidSender == null) {
                event.reply("Ваш игровой аккаунт не привязан к учетной записи банка.").setEphemeral(true).queue()
                return@thenAccept
            }

            uuidTargetFuture.thenAccept { uuidTarget ->
                if (uuidTarget == null) {
                    event.reply("Игровой аккаунт получателя не привязан к учетной записи банка.").setEphemeral(true).queue()
                    return@thenAccept
                }

                val walletSender = userDB.getDefaultWalletByUUID(uuidSender) ?: return@thenAccept
                val walletTarget = userDB.getDefaultWalletByUUID(uuidTarget) ?: return@thenAccept

                if (walletSender.toString() == "null") {
                    event.reply("У вас нет кошелька.").setEphemeral(true).queue()
                    return@thenAccept
                }
                if (walletTarget.toString() == "null") {
                    event.reply("У получателя нет доступного кошелька.").setEphemeral(true).queue()
                    return@thenAccept
                }
                if (walletSender == walletTarget) {
                    event.reply(localizationManager.getMessage("localisation.messages.out.wallet-same-thing")).setEphemeral(true).queue()
                    return@thenAccept
                }
                if (!walletDB.checkWalletStatus(walletSender)) {
                    event.reply(localizationManager.getMessage("localisation.messages.out.wallet.unavailable.sender")).setEphemeral(true).queue()
                    return@thenAccept
                }
                if (!walletDB.checkWalletStatus(walletTarget)) {
                    event.reply(localizationManager.getMessage("localisation.messages.out.wallet.unavailable.target")).setEphemeral(true).queue()
                    return@thenAccept
                }

                val senderBalance = walletDB.getWalletBalance(walletSender) ?: return@thenAccept
                val targetBalance = walletDB.getWalletBalance(walletTarget) ?: return@thenAccept

                if (senderBalance < amount) {
                    event.reply(localizationManager.getMessage("localisation.messages.out.wallet.not-balance")).setEphemeral(true).queue()
                    return@thenAccept
                }

                val limit = configPlugin.getInt("wallet-limit")
                if (targetBalance + amount > limit) {
                    val free = (targetBalance + amount - limit).toString()
                    event.reply(localizationManager.getMessage("localisation.messages.out.wallet.overflow.target", "free" to free)).setEphemeral(true).queue()
                    return@thenAccept
                }

                val currency1 = walletDB.getWalletCurrency(walletSender) ?: return@thenAccept
                val currency2 = walletDB.getWalletCurrency(walletTarget) ?: return@thenAccept
                if (currency1 != currency2) {
                    event.reply(localizationManager.getMessage("localisation.messages.out.wallet.currency.mismatch",
                        "currencyS" to currency1, "currencyT" to currency2)).setEphemeral(true).queue()
                    return@thenAccept
                }

                val senderName = userDB.getPlayerNameByUUID(uuidSender) ?: return@thenAccept
                val targetName = userDB.getPlayerNameByUUID(uuidTarget) ?: return@thenAccept

                val successful = walletDB.transferCash(
                    senderName, targetName, walletSender, walletTarget, amount.toInt(), currency1, 1, uuidSender, uuidTarget
                )

                if (successful) {
                    event.reply("Вы перевели $amount $currency1 игроку ${targetMember.asMention}.").setEphemeral(true).queue()

                    discordNotifier.sendPrivateMessage(
                        discordIDSender,
                        localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.sender",
                            "amount" to amount.toString(),
                            "currency" to currency1,
                            "target" to targetName,
                            "senderWalletID" to walletSender.toString(),
                            "targetWalletID" to walletTarget.toString())
                    )

                    discordNotifier.sendPrivateMessage(
                        discordIDTarget,
                        localizationManager.getMessage("localisation.discord.logger.transfer-successfully",
                            "walletIDSender" to walletSender.toString(),
                            "sender" to senderName,
                            "walletIDTarget" to walletTarget.toString(),
                            "target" to targetName,
                            "amount" to amount.toString(),
                            "currency" to currency1)
                    )

                    if (functions.isPlayerOnline(uuidSender)) {
                        val senderPlayer = functions.getPlayerByUUID(uuidSender) ?: return@thenAccept
                        senderPlayer.sendMessage("С вашего аккаунта выполнена операция. Подробнее в Discord.")
                    }
                    if (functions.isPlayerOnline(uuidTarget)) {
                        val targetPlayer = functions.getPlayerByUUID(uuidTarget) ?: return@thenAccept
                        targetPlayer.sendMessage(
                            localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.target",
                                "sender" to senderName,
                                "amount" to amount.toString(),
                                "currency" to currency1,
                                "senderWalletID" to walletSender.toString(),
                                "targetWalletID" to walletTarget.toString())
                        )
                    }
                } else {
                    event.reply("Ошибка операции").setEphemeral(true).queue()
                }
            }
        }
    }
}
