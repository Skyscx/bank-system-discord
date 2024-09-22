package discord.dsbot.buttonsactions.collection

import App.Companion.localized
import App.Companion.reportsDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.bukkit.configuration.file.FileConfiguration

class ReportWalletButtonsHandler(config: FileConfiguration) {

    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

    fun handle(event: ButtonInteractionEvent, parts: List<String>) {
        val reportID = parts[1].toInt()
        val id = reportsDB.getID(reportID) ?: return
        val senderName = reportsDB.getSenderName(id) ?: return
        val senderUUID = reportsDB.getSenderUUID(id) ?: return
        val senderDID = reportsDB.getSenderDID(id) ?: return
        val discordUserID = event.user.id
        val bankerPermission = "skybank.banker"
        val typeReport = reportsDB.getType(id) ?: return
        val textReport = reportsDB.getResponseText(id) ?: return
        val dateResponse = reportsDB.getDateResponse(id) ?: return
        val dateDispatch = reportsDB.getDateDispatch(id) ?: return


        event.deferEdit().queue()

        event.message.editMessageComponents().queue(
            { },
            { it.printStackTrace() }
        )

        val embedFields = mutableListOf<MessageEmbed.Field>()
        val color = when (parts[0]) {
            "reportWalletApprove" -> {
                functions.sendMessageIsPlayerOnline(senderUUID, "localisation.messages.out.wallet.report-response.successful".localized())
                discordNotifier.sendPrivateMessage(senderDID, "localisation.messages.out.wallet.report-response.successful".localized())
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.status".localized(), "localisation.report.status.accept".localized(), false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.date-response".localized(), dateResponse, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.date-dispath".localized(), dateDispatch, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.report-type".localized(), typeReport, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.report-text".localized(), textReport, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.report-sender".localized(), senderName, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.inspector".localized(), event.user.asMention, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.reportID".localized(), "$reportID", false))
                0x00FF00
            }
            "reportWalletReject" -> {
                functions.sendMessageIsPlayerOnline(senderUUID, "localisation.messages.out.wallet.report-response.unsuccessful".localized())
                discordNotifier.sendPrivateMessage(senderDID, "localisation.messages.out.wallet.report-response.unsuccessful".localized())
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.status".localized(), "localisation.report.status.reject".localized(), false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.date-response".localized(), dateResponse, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.date-dispath".localized(), dateDispatch, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.report-type".localized(), typeReport, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.report-text".localized(), textReport, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.report-sender.localized()", senderName, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.inspector", event.user.asMention, false))
                embedFields.add(MessageEmbed.Field("localisation.discord.embed.report.field.reportID".localized(), "$reportID", false))
                0x00FF00
            }
            "reportWalletContact" -> {
                // Логика для кнопки "Связаться"
                0x00FF00
            }
            else -> 0x808080
        }

        val newEmbed = discordNotifier.createEmbedMessage(
            title = "localisation.discord.embed.title.report-response".localized(),
            embedType = EmbedType.RICH,
            fields = embedFields,
            color = color
        )

        event.message.editMessageEmbeds(newEmbed).queue(
            { },
            { it.printStackTrace() }
        )
    }
}