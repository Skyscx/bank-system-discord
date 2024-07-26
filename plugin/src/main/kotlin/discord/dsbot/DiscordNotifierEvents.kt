package discord.dsbot

import database.Database
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordNotifierEvents(private val database: Database) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val componentId = event.componentId
        val parts = componentId.split(":")
        val action = parts[0]
        val walletId = parts[1].toInt()
        event.deferEdit().queue()
        when (action) {
            "acceptAccount" -> {
                event.message.delete().queue(
                    { println("Сообщение успешно удалено") },
                    { it.printStackTrace() }
                )
                event.channel.sendMessage("Запрос был одобрен! (ID MESSAGE: ${event.messageId}, ID ACCOUNT: $walletId)").queue()
                database.setVerification(walletId,1)
                database.setDeposit(walletId, 0.toString())
            }
            "rejectAccount" -> {
                event.message.delete().queue(
                    { println("Сообщение успешно удалено") },
                    { it.printStackTrace() }
                )
                event.channel.sendMessage("Запрос был отклонен! (ID MESSAGE: ${event.messageId}, ID ACCOUNT: $walletId)").queue()
                database.setVerification(walletId,-1)
            }
        }
    }
}
