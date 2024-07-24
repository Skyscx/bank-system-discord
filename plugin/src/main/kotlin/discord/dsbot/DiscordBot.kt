package discord.dsbot
import database.Database
import discord.dsbot.commands.PayCommandDiscord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class DiscordBot (private val database: Database): ListenerAdapter() {
    lateinit var jda: JDA
    fun start(token: String?) {
        jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .addEventListeners(PayCommandDiscord(database))
            .build()

        jda.awaitReady()
        updateCommands()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "hello") return

        //val channel = event.channel as MessageChannel
        //val user = event.user
        //event.reply(event.getOption("Hello, ${user.asMention}!")!!.asString).queue()
        //channel.sendMessage("Hello, ${user.asMention}!").queue()
        //event.reply("Test3").queue()

        val channel = event.channel as MessageChannel
//        val user = event.user

        // проверяем ID канала
        if (channel.idLong != PayCommandDiscord.ALLOWED_CHANNEL_ID) {
            event.reply("Эту команду можно использовать только в <#${PayCommandDiscord.ALLOWED_CHANNEL_ID}> канале.").queue()
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

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            Commands.slash("hello", "Says hello to the user"),
            Commands.slash("pay", "My custom command")
        )

        val guild = jda.getGuildById("1265001474870612068")
        guild?.updateCommands()?.addCommands(commands)?.queue()
    }



}
