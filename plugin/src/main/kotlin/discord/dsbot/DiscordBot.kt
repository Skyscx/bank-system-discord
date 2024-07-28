package discord.dsbot
import data.Database
import discord.dsbot.commands.PayCommandDiscord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.bukkit.configuration.file.FileConfiguration

class DiscordBot private constructor(private val database: Database, private val config: FileConfiguration) {
    companion object {
        @Volatile
        private var instance: DiscordBot? = null

        fun getInstance(database: Database, config: FileConfiguration): DiscordBot =
            instance ?: synchronized(this) {
                instance ?: DiscordBot(database, config).also { instance = it }
            }
    }

    lateinit var jda: JDA

    fun start(token: String?) {
        jda = JDABuilder.createDefault(token)
            .addEventListeners(PayCommandDiscord(database, config))
            .addEventListeners(DiscordNotifierEvents(database))
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

    fun mentionUserById(userId: String): User? {
        return try {
            val userIdLong = userId.toLong()
            jda.getUserById(userIdLong)
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun getJDA(): JDA {
        return jda
    }
}
