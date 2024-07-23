
import net.dv8tion.jda.api.events.interaction.command.SlashCommandEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class DiscordBot : ListenerAdapter() {
    private lateinit var jda: JDA

    fun start(token: String) {
        jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .build()

        updateCommands()
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.name != "hello") return

        val channel = event.channel as MessageChannel
        val user = event.user

        channel.sendMessage("Hello, ${user.asMention}!").queue()
    }

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            Commands.slash("hello", "Says hello to the user")
        )

        val guild = jda.getGuildById("your guild id here") // замените на идентификатор вашего сервера
        if (guild != null) {
            guild.updateCommands().addCommands(commands).queue()
        } else {
            jda.updateCommands().addCommands(commands).queue()
        }
    }
}

fun main(args: Array<String>) {
    val token = "your bot token here"
    val bot = DiscordBot()
    bot.start(token)
}
