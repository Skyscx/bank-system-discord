package discord.dsbot.buttonsactions

import discord.dsbot.buttonsactions.collection.BankerVerificationsButtonsHandler
import discord.dsbot.buttonsactions.collection.ReportWalletButtonsHandler
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class ButtonInteractionHandler(config: FileConfiguration) : ListenerAdapter() {

    private val reportWalletButtonsHandler = ReportWalletButtonsHandler(config)
    private val bankerVerificationsButtonsHandler = BankerVerificationsButtonsHandler(config)

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            if (event.isAcknowledged) return

            val componentId = event.componentId
            val parts = componentId.split(":")
            val action = parts[0]

            when (action) {
                "reportWalletApprove", "reportWalletReject", "reportWalletContact" -> reportWalletButtonsHandler.handle(event, parts)
                "acceptAccount", "rejectAccount" -> bankerVerificationsButtonsHandler.handle(event, parts)
                else -> println("Unknown action: $action")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error processing button interaction: ${e.message}")
        }
    }
}