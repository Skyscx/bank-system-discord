package discord.dsbot.buttonsactions.collection

import App.Companion.discordBot
import App.Companion.historyDB
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.bukkit.configuration.file.FileConfiguration

class BankerVerificationsButtonsHandler(config: FileConfiguration) {

    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

    fun handle(event: ButtonInteractionEvent, parts: List<String>) {
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

        event.deferEdit().queue()

        event.message.editMessageComponents().queue(
            { },
            { it.printStackTrace() }
        )

        val playerNameTarget = userDB.getPlayerNameByUUID(uuid.toString()) ?: return
        val didTarget = userDB.getDiscordIDbyUUID(uuid.toString()) ?: return
        val mentionTarget = discordBot?.getMentionUser(didTarget)

        val embedFields = mutableListOf<MessageEmbed.Field>()
        val color = when (parts[0]) {
            "acceptAccount" -> {
                if (verificationDatabase == 0) {
                    walletDB.setVerificationWallet(walletId, 1)
                    walletDB.setDepositWallet(walletId, "0")
                    walletDB.setInspectorWallet(walletId, discordUserID)
                    walletDB.setVerificationWalletDate(walletId)
                    historyDB.insertBankHistory(
                        typeOperation = "OPEN_WALLET",
                        senderName = playerNameTarget,
                        senderWalletID = walletId,
                        uuidSender = uuid.toString(),
                        amount = 15, // todo: Сделать из ДБ
                        currency = "DIAMOND_ORE", //todo: Сделать из дб
                        status = 1,

                        uuidTarget = "null",
                        comment =  "null",
                        targetWalletID = 0,
                        targetName = "null"
                    )
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
                    historyDB.insertBankHistory(
                        typeOperation = "ATTEMPT_OPEN_WALLET",
                        senderName = playerNameTarget,
                        senderWalletID = walletId,
                        uuidSender = uuid.toString(),
                        amount = 0,
                        currency = "null",
                        status = 1,

                        uuidTarget = "null",
                        comment =  "null",
                        targetWalletID = 0,
                        targetName = "null"
                    )
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

        val newEmbed = discordNotifier.createEmbedMessage(
            title = "Рассмотрение заявки!",
            embedType = EmbedType.RICH,
            color = color,
            fields = embedFields
        )

        event.message.editMessageEmbeds(newEmbed).queue(
            { },
            { it.printStackTrace() }
        )
    }
}
