package discord.dsbot.commands

import database.Database
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class PayCommandDiscord (private val database: Database) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "pay") return

        val channel = event.channel as MessageChannel
        val user = event.user

        // проверяем ID канала
        if (channel.idLong != ALLOWED_CHANNEL_ID) {
            event.reply("Эту команду можно использовать только в <#$ALLOWED_CHANNEL_ID> канале.").queue()
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

        val uuidSender = database.getUUIDforDiscordID(discordIDSender)
        if (uuidSender == null) {
            event.reply("У вас нет привязанного UUID.").queue()
            return
        }
        println("UUID S - $uuidSender")
        val uuidTarget = database.getUUIDforDiscordID(discordIDTarget)
        if (uuidTarget == null) {
            event.reply("У целевого игрока нет привязанного UUID.").queue()
            return
        }
        println("UUID T - $uuidTarget")

        val senderBalance = database.getPlayerBalance(uuidSender)
        println("S BALANCE - $senderBalance")
        if (senderBalance < amount) {
            user.openPrivateChannel().queue { channel ->
                event.reply("У вас недостаточно средств.").queue()
            }
            return
        }

        val newSenderBalance = senderBalance - amount
        println("N S B - $newSenderBalance")
        val newTargetBalance = database.getPlayerBalance(uuidTarget) + amount
        println("N T B - $newTargetBalance")


        database.setPlayerBalance(uuidSender, newSenderBalance.toInt())
        database.setPlayerBalance(uuidTarget, newTargetBalance.toInt())
        user.openPrivateChannel().queue { channel ->
            channel.sendMessage("Вы перевели $amount монет игроку ${targetMember.asMention}.").queue()
        }
        targetMember.user.openPrivateChannel().queue { channel ->
            channel.sendMessage("Игрок ${user.asMention} перевел вам $amount монет.").queue()
        }
        event.reply("Команда выполнена успешно.").queue()

    }
    companion object {
        const val ALLOWED_CHANNEL_ID = 1265343553614250078
    }
}