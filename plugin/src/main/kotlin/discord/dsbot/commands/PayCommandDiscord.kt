package discord.dsbot.commands

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class PayCommandDiscord : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "pay") return

        val channel = event.channel as MessageChannel
        val user = event.user

        // проверяем ID канала
        if (channel.idLong != ALLOWED_CHANNEL_ID) {
            channel.sendMessage("Эту команду можно использовать только в <#$ALLOWED_CHANNEL_ID> канале.").queue()
            return
        }

        channel.sendMessage("Test, ${user.asMention}! Soon release command.").queue()
    }

    companion object {
        const val ALLOWED_CHANNEL_ID = 1265343553614250078
    }
}