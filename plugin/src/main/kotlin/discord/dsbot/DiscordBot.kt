package discord.dsbot
import App.Companion.localized
import discord.dsbot.buttonsactions.ButtonInteractionHandler
import discord.dsbot.commands.BalanceCommandDiscord
import discord.dsbot.commands.TransferCommandDiscord
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
        fun getJDA(): JDA? {
            return instance?.jda
        }
    }

    lateinit var jda: JDA

    fun start(token: String?) {
        jda = JDABuilder.createDefault(token).build()
        jda.awaitReady()
        updateCommands()
        registerEventListeners()
    }

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            //Commands.slash("link-bank", "Регистрация в банковской системе"),
            Commands.slash("balance", "localisation.discord.command.balance.description".localized()),
            Commands.slash("transfer", "localisation.discord.command.transfer.description".localized())
                .addOption(OptionType.USER, "user", "localisation.discord.command.transfer.options.user".localized(), true)
                .addOption(OptionType.INTEGER, "amount", "localisation.discord.command.transfer.options.amount".localized(), true)
                .addOption(OptionType.STRING, "comment", "localisation.discord.command.transfer.options.comment".localized(), false)
        )

        jda.updateCommands().addCommands(commands).queue({
        }, {
            it.printStackTrace()
            println("Failed to update global commands: ${it.message}")
        })

    }

    private fun registerEventListeners() {
        jda.addEventListener(BalanceCommandDiscord(config))
        jda.addEventListener(TransferCommandDiscord(config))
        jda.addEventListener(ButtonInteractionHandler(config));
    }
    fun getMentionUser(discordID: String): String {
        val mention = UserSnowflake.fromId(discordID).asMention
        return mention
    }
}
