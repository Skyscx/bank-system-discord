package discord.dsbot.commands

import database.Database
import discord.Functions
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class PayCommandDiscord (private val database: Database, config: FileConfiguration) : ListenerAdapter() {
    private val functions = Functions()
    private val allowedСhannelId = config.getLong("allowed-channel-id-for-bank-commands")
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "pay") return

        val channel = event.channel as MessageChannel
        val user = event.user

        // проверяем ID канала
        if (channel.idLong != allowedСhannelId) {
            event.reply("Эту команду можно использовать только в <#$allowedСhannelId> канале.").queue()
            return
        }

        //Получение аргументов
        val targetMember = event.getOption("user")?.asMember ?: return
        println("TargetMember - $targetMember")
        val amount = event.getOption("amount")?.asLong ?: return
        println("Amount - $amount")

        // проверяем, что сумма перевода положительная
        if (amount <= 0) {
            event.reply("Сумма перевода должна быть положительной.").queue()
            return
        }
        // Операция перевода
        val discordIDSender = user.id
        println("DIDSender - $discordIDSender")
        val discordIDTarget = targetMember.id
        println("DIDTarget - $discordIDTarget")

        val uuidSenderFuture = database.getUUIDbyDiscordID(discordIDSender)
        val uuidTargetFuture = database.getUUIDbyDiscordID(discordIDTarget)

        uuidSenderFuture.thenAccept { uuidSender ->
            if (uuidSender == null) {
                event.reply("Ваш игровой аккаунт не привязан к учетной записи банка.").queue()
                return@thenAccept
            }
            uuidTargetFuture.thenAccept { uuidTarget ->
                if (uuidTarget == null) {
                    event.reply("Игровой аккаунт получателя не привязан к учетной записи банка.").queue()
                    return@thenAccept
                }
                val senderBalance = database.getPlayerBalance(uuidSender)
                if (senderBalance < amount) {
                    user.openPrivateChannel().queue { channel ->
                        event.reply("У вас недостаточно средств.").queue()
                    }
                    return@thenAccept
                }
                val newSenderBalance = senderBalance - amount
                val newTargetBalance = database.getPlayerBalance(uuidTarget) + amount
                database.setPlayerBalance(uuidSender, newSenderBalance.toInt())
                database.setPlayerBalance(uuidTarget, newTargetBalance.toInt())
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("Вы перевели $amount монет игроку ${targetMember.asMention}.").queue()
                }
                targetMember.user.openPrivateChannel().queue { channel ->
                    channel.sendMessage("Игрок ${user.asMention} перевел вам $amount монет.").queue()
                }
                if (functions.isPlayerOnline(uuidSender)){
                    val player = functions.getPlayerByUUID(uuidSender)
                    if (player != null) { functions.sendMessagePlayer(player, "Sender") }
                }
                if (functions.isPlayerOnline(uuidTarget)){
                    val player = functions.getPlayerByUUID(uuidTarget)
                    if (player != null) { functions.sendMessagePlayer(player, "Target") }
                }

                event.reply("Команда выполнена успешно.").queue()
            }
        }

    }
}