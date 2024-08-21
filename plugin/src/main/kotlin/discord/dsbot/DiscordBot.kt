package discord.dsbot
//import discord.dsbot.commands.PayCommandDiscord
import data.database.DatabaseManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.bukkit.configuration.file.FileConfiguration

class DiscordBot private constructor(dbManager: DatabaseManager, private val config: FileConfiguration) {
    companion object {
        @Volatile
        private var instance: DiscordBot? = null

        fun getInstance(dbManager: DatabaseManager, config: FileConfiguration): DiscordBot =
            instance ?: synchronized(this) {
                instance ?: DiscordBot(dbManager, config).also { instance = it }
            }
    }

    lateinit var jda: JDA
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!

    fun start(token: String?) {
        jda = JDABuilder.createDefault(token)
            //.addEventListeners(PayCommandDiscord(database, config))
            .addEventListeners(DiscordNotifierEvents())
            // .addEventListeners(CommandAccountBinder(database, config)) TODO:Функционал отключен
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
    fun getMentionUser(discordID: String): String {
        val mention = UserSnowflake.fromId(discordID).asMention
        return mention
    }
    fun getJDA(): JDA {
        return jda
    }
}
