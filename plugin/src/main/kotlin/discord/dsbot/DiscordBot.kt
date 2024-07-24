package discord.dsbot
import database.Database
import discord.dsbot.commands.PayCommandDiscord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
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
        val channel = event.channel as MessageChannel
        if (channel.idLong != PayCommandDiscord.ALLOWED_CHANNEL_ID) {
            event.reply("Эту команду можно использовать только в <#${PayCommandDiscord.ALLOWED_CHANNEL_ID}> канале.").queue()
            return
        }
        event.reply("Команда выполнена успешно.").queue()


    }

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            Commands.slash("hello", "Says hello to the user"),
            Commands.slash("pay", "My custom command")
                .addOption(OptionType.USER, "user", "Целевой игрок", true)
                .addOption(OptionType.INTEGER, "amount", "Сумма перевода", true)
        )

        val guild = jda.getGuildById("1265001474870612068")
        guild?.updateCommands()?.addCommands(commands)?.queue()
    }



}
