package discord.dsbot.buttonsactions.collection

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
                functions.sendMessageIsPlayerOnline(senderUUID, "Ваша жалоба рассмотрена. (Принята)")
                discordNotifier.sendPrivateMessage(senderDID, "Ваша жалоба рассмотрена. (Принята)")
                embedFields.add(MessageEmbed.Field("Статус", "Одобрено", false))
                embedFields.add(MessageEmbed.Field("Дата рассмотрения", "$dateResponse", false))
                embedFields.add(MessageEmbed.Field("Дата получения", "$dateDispatch", false))
                embedFields.add(MessageEmbed.Field("Тип жалобы", "$typeReport", false))
                embedFields.add(MessageEmbed.Field("Текст жалобы", "$textReport", false))
                embedFields.add(MessageEmbed.Field("Жаловался", "$senderName", false))
                embedFields.add(MessageEmbed.Field("Рассмотрел", event.user.asMention, false))
                embedFields.add(MessageEmbed.Field("Идентификатор", "$reportID", false))
                0x00FF00
            }
            "reportWalletReject" -> {
                functions.sendMessageIsPlayerOnline(senderUUID, "Ваша жалоба рассмотрена. (Отклонена)")
                discordNotifier.sendPrivateMessage(senderDID, "Ваша жалоба рассмотрена. (Отклонена)")
                embedFields.add(MessageEmbed.Field("Статус", "Отклонено", false))
                embedFields.add(MessageEmbed.Field("Дата рассмотрения", "$dateResponse", false))
                embedFields.add(MessageEmbed.Field("Дата получения", "$dateDispatch", false))
                embedFields.add(MessageEmbed.Field("Тип жалобы", "$typeReport", false))
                embedFields.add(MessageEmbed.Field("Текст жалобы", "$textReport", false))
                embedFields.add(MessageEmbed.Field("Жаловался", "$senderName", false))
                embedFields.add(MessageEmbed.Field("Рассмотрел", event.user.asMention, false))
                embedFields.add(MessageEmbed.Field("Идентификатор", "$reportID", false))
                0x00FF00
            }
            "reportWalletContact" -> {
                // Логика для кнопки "Связаться"
                0x00FF00
            }
            else -> 0x808080
        }

        val newEmbed = discordNotifier.createEmbedMessage(
            title = "Рассмотрение жалобы!",
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