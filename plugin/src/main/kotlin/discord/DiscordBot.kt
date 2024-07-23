
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class DiscordBot : ListenerAdapter() {
    lateinit var jda: JDA
    fun start(token: String?) {
        jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .addEventListeners(MyCommand())
            .build()

        jda.awaitReady()
        updateCommands()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "hello") return

        val channel = event.channel as MessageChannel
        val user = event.user

        channel.sendMessage("Hello, ${user.asMention}!").queue()
    }

    private fun updateCommands() {
        val commands: List<SlashCommandData> = listOf(
            Commands.slash("hello", "Says hello to the user"),
            Commands.slash("mycommand", "My custom command")
        )

        val guild = jda.getGuildById("1265001474870612068")
        guild?.updateCommands()?.addCommands(commands)?.queue()
    }

    class MyCommand : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            if (event.name != "mycommand") return

            val channel = event.channel as MessageChannel
            val user = event.user

            channel.sendMessage("Hello, ${user.asMention}! This is my command.").queue()
        }
    }

}
