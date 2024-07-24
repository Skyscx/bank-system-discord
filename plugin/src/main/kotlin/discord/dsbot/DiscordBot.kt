package discord.dsbot
import database.Database
import discord.dsbot.commands.PayCommandDiscord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.bukkit.configuration.file.FileConfiguration

class DiscordBot (private val database: Database, private val config: FileConfiguration): ListenerAdapter() {
    lateinit var jda: JDA
    fun start(token: String?) {
        jda = JDABuilder.createDefault(token)
            .addEventListeners(PayCommandDiscord(database, config))
 //           .addEventListeners(CommandAccountBinder(database, config))
 //           .addEventListeners(ModalListener(database,config))
            .build()

        jda.awaitReady()
        updateCommands()
    }

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            Commands.slash("link-bank", "Регистрация в банковской системе"),
            Commands.slash("pay", "My custom command")
                .addOption(OptionType.USER, "user", "Целевой игрок", true)
                .addOption(OptionType.INTEGER, "amount", "Сумма перевода", true)
        )

        val guild = jda.getGuildById("1265001474870612068")
        guild?.updateCommands()?.addCommands(commands)?.queue()
    }



}
