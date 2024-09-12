package discord.dsbot.buttonsactions

import App.Companion.reportsDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class ReportWalletButtons(config: FileConfiguration): ListenerAdapter() {

    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            val componentId = event.componentId
            val parts = componentId.split(":")
            val action = parts[0]
            val reportID = parts[1].toInt()
            val id = reportsDB.getID(reportID) ?: return
            val senderName = reportsDB.getSenderName(id) ?: return
            val senderUUID = reportsDB.getSenderUUID(id) ?: return
            val senderDID = reportsDB.getSenderDID(id) ?: return


            val discordUserID = event.user.id

            val bankerPermission = "skybank.banker"
            if (event.isAcknowledged) return
            event.deferEdit().queue()
            event.message.editMessageComponents().queue(
                {  },
                { it.printStackTrace() }
            )

            val embedFields = mutableListOf<MessageEmbed.Field>()
            val color = when (action) {
                "reportWalletApprove" -> { //todo: ОДОБРЕНО
                    functions.sendMessageIsPlayerOnline(senderUUID, "Ваша жалоба рассмотрена. (Принята)") // Если игрок онлайн ему это выйдет
                    discordNotifier.sendPrivateMessage(senderDID, "Ваша жалоба рассмотрена. (Принята)")
                    //todo: Подумать нужно ли сообщение для банкиров онлайн???
                    //functions.sendMessageIsPlayerHavePermission(uuid, bankerPermission, "Кошелек $walletId одобрил ${event.user.name}")
                    embedFields.add(MessageEmbed.Field("Статус", "Одобрено", false))
                    embedFields.add(MessageEmbed.Field("Рассмотрел", event.user.asMention, false))
                    embedFields.add(MessageEmbed.Field("Идентификатор", "$reportID", false))
                    0x00FF00
                }
                "reportWalletReject" -> { // todo: отклонено
                    functions.sendMessageIsPlayerOnline(senderUUID, "Ваша жалоба рассмотрена. (Принята)") // Если игрок онлайн ему это выйдет
                    discordNotifier.sendPrivateMessage(senderDID, "Ваша жалоба рассмотрена. (Принята)")
                    //todo: Подумать нужно ли сообщение для банкиров онлайн???
                    //functions.sendMessageIsPlayerHavePermission(uuid, bankerPermission, "Кошелек $walletId одобрил ${event.user.name}")
                    embedFields.add(MessageEmbed.Field("Статус", "Отклонено", false))
                    embedFields.add(MessageEmbed.Field("Рассмотрел", event.user.asMention, false))
                    embedFields.add(MessageEmbed.Field("Идентификатор", "$reportID", false))
                    0x00FF00
                }
                "reportWalletContact" -> {
                    // Логика для кнопки "Связаться"
                    //todo: создание тикета
                    0x00FF00
                }
                else -> 0x808080
            }

            // Создание нового MessageEmbed
            val newEmbed = discordNotifier.createEmbedMessage(
                null,
                "Рассмотрение жалобы!",
                null,
                EmbedType.RICH,
                null,
                color,
                null,
                null,
                null,
                null,
                null,
                null,
                embedFields
            )

            event.message.editMessageEmbeds(newEmbed).queue(
                { },
                { it.printStackTrace() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error processing button interaction: ${e.message}")
        }
    }
}