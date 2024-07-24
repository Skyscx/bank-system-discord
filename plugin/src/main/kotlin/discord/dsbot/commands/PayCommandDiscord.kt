package discord.dsbot.commands

import database.Database
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class PayCommandDiscord (private val database: Database) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

        if (event.name != "pay") return

        val channel = event.channel as MessageChannel
//        val user = event.user

        // проверяем ID канала
        if (channel.idLong != ALLOWED_CHANNEL_ID) {
            event.reply("Эту команду можно использовать только в <#$ALLOWED_CHANNEL_ID> канале.").queue()
            return
        }

        //Получение аргументов
//        val targetMember = event.getOption("user")?.asMember ?: return
//        val amount = event.getOption("amount")?.asLong ?: return

        // проверяем, что сумма перевода положительная
//        if (amount <= 0) {
//            event.reply("Сумма перевода должна быть положительной.").queue()
//            return
//        }
//

        // Операция перевода
//        val discordIDUser = user.id
//        val uuid = database.getUUIDforDiscordID(discordIDUser)
//
//        val senderBalance = database.getPlayerBalance(uuid)
//        if (senderBalance < amount) {
//            user.openPrivateChannel().queue { channel ->
//                event.reply("У вас недостаточно средств.").queue()
//            }
//            return
//        }
//
//        val newSenderBalance = senderBalance - amount
//        val newTargetBalance = database.getPlayerBalance(targetMember.id) + amount
//
//        database.setPlayerBalance(uuid, newSenderBalance.toInt())
//        database.setPlayerBalance(targetMember.id, newTargetBalance.toInt())
//
//        user.openPrivateChannel().queue { channel ->
//            channel.sendMessage("Вы перевели $amount монет игроку ${targetMember.asMention}.").queue()
//        }
//
//        targetMember.user.openPrivateChannel().queue { channel ->
//            channel.sendMessage("Игрок ${user.asMention} перевел вам $amount монет.").queue()
//        }

        event.reply("Команда выполнена успешно.").queue()
    }

    companion object {
        const val ALLOWED_CHANNEL_ID = 1265343553614250078
    }
}