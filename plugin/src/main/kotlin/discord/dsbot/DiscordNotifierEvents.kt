package discord.dsbot

import App.Companion.discordBot
import App.Companion.userDB
import App.Companion.walletDB
import functions.Functions
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class DiscordNotifierEvents(config: FileConfiguration): ListenerAdapter() {

    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            val componentId = event.componentId
            val parts = componentId.split(":")
            val action = parts[0]
            val walletId = parts[1].toInt()
            val verificationDatabase = walletDB.getVerificationWallet(walletId)
            val discordUserID = event.user.id
            val uuid = walletDB.getUUIDbyWalletID(walletId)
            val bankerPermission = "skybank.banker"
            val status = when (verificationDatabase) {
                1 -> "Запрос был одобрен."
                -1 -> "Запрос был отклонен."
                else -> "Статус запроса неизвестен."
            }
            println("Verification status for wallet ID $walletId: $verificationDatabase")

            // Проверка, было ли взаимодействие уже подтверждено
            if (event.isAcknowledged) {
                println("Interaction already acknowledged")
                return
            }

            event.deferEdit().queue()

            event.message.editMessageComponents().queue(
                { println("Компоненты сообщения успешно удалены") },
                { it.printStackTrace() }
            )

            val playerNameTarget = userDB.getPlayerNameByUUID(uuid.toString()) ?: return
            val didTarget = userDB.getDiscordIDbyUUID(uuid.toString()) ?: return
            val mentionTarget = discordBot?.getMentionUser(didTarget)

            val embedFields = mutableListOf<MessageEmbed.Field>()
            val color = when (action) {
                "acceptAccount" -> {
                    if (verificationDatabase == 0) {
                        walletDB.setVerificationWallet(walletId, 1)
                        walletDB.setDepositWallet(walletId, "0")
                        walletDB.setInspectorWallet(walletId, discordUserID)
                        walletDB.setVerificationWalletDate(walletId)
                        functions.sendMessageIsPlayerOnline(uuid!!, "Ваш запрос одобрили!")
                        functions.sendMessageIsPlayerHavePermission(uuid, bankerPermission, "Кошелек $walletId одобрил ${event.user.name}")
                        embedFields.add(MessageEmbed.Field("Статус", "Запрос был одобрен!", false))
                        embedFields.add(MessageEmbed.Field("Одобрил", event.user.asMention, false))
                        embedFields.add(MessageEmbed.Field("Одобрен для", "$mentionTarget (MC: $playerNameTarget)", false))
                        embedFields.add(MessageEmbed.Field("Кошелек", "$walletId", false))
                        0x00FF00
                    } else {
                        val discordIDInspector = walletDB.getInspectorWallet(walletId) ?: return
                        val mentionInspector = discordBot?.getMentionUser(discordIDInspector)
                        val verificationDate = walletDB.getVerificationWalletDate(walletId)
                        embedFields.add(MessageEmbed.Field("Статус", "Данный запрос уже был рассмотрен в игре!", false))
                        embedFields.add(MessageEmbed.Field("Рассмотрел", mentionInspector, false))
                        embedFields.add(MessageEmbed.Field("Рассмотрен для", "$mentionTarget (MC: $playerNameTarget)", false))
                        embedFields.add(MessageEmbed.Field("Кошелек", "$walletId", false))
                        embedFields.add(MessageEmbed.Field("Дата рассмотрения", "`$verificationDate`", false))
                        embedFields.add(MessageEmbed.Field("Статус запроса", status, false))
                        0x808080
                    }
                }
                "rejectAccount" -> {
                    if (verificationDatabase == 0) {
                        walletDB.setVerificationWallet(walletId, -1)
                        walletDB.setInspectorWallet(walletId, event.user.id)
                        walletDB.setVerificationWalletDate(walletId)
                        functions.sendMessageIsPlayerOnline(walletDB.getUUIDbyWalletID(walletId).toString(), "Ваш запрос отклонили!")
                        embedFields.add(MessageEmbed.Field("Статус", "Запрос был отклонен!", false))
                        embedFields.add(MessageEmbed.Field("Отклонил", event.user.asMention, false))
                        embedFields.add(MessageEmbed.Field("Отклонен для", "$mentionTarget (MC: $playerNameTarget)", false))
                        embedFields.add(MessageEmbed.Field("Кошелек", "$walletId", false))
                        0xFF0000
                    } else {
                        val discordIDInspector = walletDB.getInspectorWallet(walletId) ?: return
                        val mentionInspector = discordBot?.getMentionUser(discordIDInspector)
                        val verificationDate = walletDB.getVerificationWalletDate(walletId)
                        embedFields.add(MessageEmbed.Field("Статус", "Данный запрос уже был рассмотрен в игре!", false))
                        embedFields.add(MessageEmbed.Field("Рассмотрел", mentionInspector, false))
                        embedFields.add(MessageEmbed.Field("Рассмотрен для", "$mentionTarget (MC: $playerNameTarget)", false))
                        embedFields.add(MessageEmbed.Field("Кошелек", "$walletId", false))
                        embedFields.add(MessageEmbed.Field("Дата рассмотрения", "`$verificationDate`", false))
                        embedFields.add(MessageEmbed.Field("Статус запроса", status, false))
                        0x808080
                    }
                }
                else -> 0x808080
            }

            // Создание нового MessageEmbed
            val newEmbed = discordNotifier.createEmbedMessage(
                null,
                "Рассмотрение заявки!",
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

            // Редактирование сообщения с новым Embed
            event.message.editMessageEmbeds(newEmbed).queue(
                { println("Сообщение отправлено") },
                { it.printStackTrace() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error processing button interaction: ${e.message}")
        }
    }
}
