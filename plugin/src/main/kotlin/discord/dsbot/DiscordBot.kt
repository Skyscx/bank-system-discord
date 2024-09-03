package discord.dsbot
//import discord.dsbot.commands.PayCommandDiscord
import discord.dsbot.commands.BalanceCommandDiscord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.bukkit.configuration.file.FileConfiguration

class DiscordBot private constructor(private val config: FileConfiguration) {
    companion object {
        @Volatile
        private var instance: DiscordBot? = null

        fun getInstance(config: FileConfiguration): DiscordBot =
            instance ?: synchronized(this) {
                instance ?: DiscordBot(config).also { instance = it }
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
            .addEventListeners(BalanceCommandDiscord(config))
            .addEventListeners(DiscordNotifierEvents())
            // .addEventListeners(CommandAccountBinder(database, config)) TODO:Функционал отключен
            .build()
        updateCommands()
        jda.awaitReady()
    }

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            //Commands.slash("link-bank", "Регистрация в банковской системе"),
//            Commands.slash("pay", "My custom command")
//                .addOption(OptionType.USER, "user", "Целевой игрок", true)
//                .addOption(OptionType.INTEGER, "amount", "Сумма перевода", true),
            Commands.slash("balance", "Узнать баланс кошелька"),
            Commands.slash("transfer", "Переводы  между игроками")
                .addOption(OptionType.USER, "user", "Получатель", true)
                .addOption(OptionType.INTEGER, "amount", "Сумма перевода", true)
        )

        jda.updateCommands().addCommands(commands).queue({
            println("Global commands updated successfully.")
        }, {
            it.printStackTrace()
            println("Failed to update global commands: ${it.message}")
        })

    }
    fun getMentionUser(discordID: String): String {
        val mention = UserSnowflake.fromId(discordID).asMention
        return mention
    }
    fun getJDA(): JDA {
        return jda
    }
}
