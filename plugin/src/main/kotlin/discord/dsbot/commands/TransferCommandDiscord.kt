package discord.dsbot.commands

import App.Companion.configPlugin
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class TransferCommandDiscord(config: FileConfiguration) : ListenerAdapter() {
    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "transfer") return
        val user = event.user
        val targetMember = event.getOption("user")?.asMember ?: return
        val amount = event.getOption("amount")?.asLong ?: return
        val comment = event.getOption("comment")?.asString ?: return

        if (amount <= 0) {
            event.reply("localisation.discord.out.amount-incorrect".localized()).setEphemeral(true).queue()
            return
        }

        // Операция перевода
        val discordIDSender = user.id
        val discordIDTarget = targetMember.id

        val uuidSenderFuture = userDB.getUUIDbyDiscordID(discordIDSender)
        val uuidTargetFuture = userDB.getUUIDbyDiscordID(discordIDTarget)

        uuidSenderFuture.thenAccept sender@ { uuidSender ->
            if (uuidSender == null) {
                event.reply("localisation.discord.out.user".localized()).setEphemeral(true).queue()
                return@sender
            }

            uuidTargetFuture.thenAccept target@ { uuidTarget ->
                if (uuidTarget == null) {
                    event.reply("localisation.discord.out.user".localized()).setEphemeral(true).queue()
                    return@target
                }

                handleTransfer(event, uuidSender, uuidTarget, discordIDSender, discordIDTarget, amount, targetMember, comment)
            }
        }
    }

    private fun handleTransfer(
        event: SlashCommandInteractionEvent,
        uuidSender: String,
        uuidTarget: String,
        discordIDSender: String,
        discordIDTarget: String,
        amount: Long,
        targetMember: Member,
        comment: String
    ) {
        val walletSender = userDB.getDefaultWalletByUUID(uuidSender) ?: return
        val walletTarget = userDB.getDefaultWalletByUUID(uuidTarget) ?: return

        if (walletSender.toString() == "null") {
            event.reply("localisation.discord.out.invalid-wallet.sender".localized()).setEphemeral(true).queue()
            return
        }
        if (walletTarget.toString() == "null") {
            event.reply("localisation.discord.out.invalid-wallet.target".localized()).setEphemeral(true).queue()
            return
        }
        if (walletSender == walletTarget) {
            event.reply("localisation.discord.out.wallet-same-thing".localized()).setEphemeral(true).queue()
            return
        }
        if (!walletDB.checkWalletStatus(walletSender)) {
            event.reply("localisation.discord.out.wallet.unavailable.sender".localized()).setEphemeral(true).queue()
            return
        }
        if (!walletDB.checkWalletStatus(walletTarget)) {
            event.reply("localisation.discord.out.wallet.unavailable.target".localized()).setEphemeral(true).queue()
            return
        }

        val senderBalance = walletDB.getWalletBalance(walletSender) ?: return
        val targetBalance = walletDB.getWalletBalance(walletTarget) ?: return

        if (senderBalance < amount) {
            event.reply("localisation.discord.out.wallet.not-balance".localized()).setEphemeral(true).queue()
            return
        }

        val limit = configPlugin.getInt("wallet-limit")
        if (targetBalance + amount > limit) {
            val free = (targetBalance + amount - limit).toString()
            event.reply("localisation.discord.out.wallet.overflow.target".localized("free" to free)).setEphemeral(true).queue()
            return
        }

        val currency1 = walletDB.getWalletCurrency(walletSender) ?: return
        val currency2 = walletDB.getWalletCurrency(walletTarget) ?: return
        if (currency1 != currency2) {
            event.reply("localisation.discord.out.wallet.currency.mismatch".localized("currencyS" to currency1, "currencyT" to currency2)).setEphemeral(true).queue()
            return
        }

        val senderName = userDB.getPlayerNameByUUID(uuidSender) ?: return
        val targetName = userDB.getPlayerNameByUUID(uuidTarget) ?: return

        val successful = walletDB.transferCash(
            senderName, targetName, walletSender, walletTarget, amount.toInt(), currency1, 1, uuidSender, uuidTarget, comment
        )

        if (successful) {
            event.reply("localisation.discord.out.wallet.transfer-successfully.sender".localized(
                "amount" to amount.toString(), "currency" to currency1, "target" to targetMember.asMention, "comment" to comment )).setEphemeral(true).queue()

            discordNotifier.sendPrivateMessage(
                discordIDSender,
                "localisation.discord.out.wallet.transfer-successfully.sender".localized(
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "target" to targetName,
                    "comment" to comment)
            )

            discordNotifier.sendPrivateMessage(
                discordIDTarget,
                "localisation.discord.out.wallet.transfer-successfully.target".localized(
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "target" to targetName,
                    "comment" to comment)
            )

            if (functions.isPlayerOnline(uuidSender)) {
                val senderPlayer = functions.getPlayerByUUID(uuidSender) ?: return
                senderPlayer.sendMessage("localisation.messages.out.transfer.ds-command.todo".localized())
            }
            if (functions.isPlayerOnline(uuidTarget)) {
                val targetPlayer = functions.getPlayerByUUID(uuidTarget) ?: return
                targetPlayer.sendMessage(
                    "localisation.discord.out.wallet.transfer-successfully.target".localized(
                        "sender" to senderName,
                        "amount" to amount.toString(),
                        "currency" to currency1,
                        "comment" to comment)
                )
            }
            // Сообщение лог в дискорд
            discordNotifier.sendMessageChannelLog("localisation.discord.logger.transfer-successfully".localized(
                "walletIDSender" to walletSender.toString(),
                "sender" to senderName,
                "walletIDTarget" to walletTarget.toString(),
                "target" to targetName,
                "amount" to amount.toString(),
                "currency" to currency1,
                "comment" to comment))
        } else {
            event.reply("localisation.error.system-error".localized()).setEphemeral(true).queue()
        }
    }
}
