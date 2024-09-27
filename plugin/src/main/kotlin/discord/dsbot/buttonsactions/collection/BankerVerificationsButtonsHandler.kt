package discord.dsbot.buttonsactions.collection

import App.Companion.discordBot
import App.Companion.historyDB
import App.Companion.localized
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
            1 -> "localisation.verification.status.accept".localized()
            -1 -> "localisation.verification.status.reject".localized()
            else -> "localisation.verification.status.other".localized()
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
                    functions.sendMessageIsPlayerOnline(uuid!!, "localisation.messages.out.wallet.verification.successful".localized())
                    functions.sendMessageIsPlayerHavePermission(uuid, bankerPermission, "localisation.messages.out.banker.notifier.verification.accept".localized( "walletId" to walletId.toString(), "name" to event.user.name)) //todo: Сделать видимым для всех банкиров и админов
                    discordNotifier.sendMessageChannelLog("localisation.discord.logger.open-successfully".localized("player" to playerNameTarget))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.status".localized(), status, false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.inspector.accept".localized(), event.user.asMention, false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.target.accept".localized(), "$mentionTarget (MC: $playerNameTarget)", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.wallet".localized(), "$walletId", false))
                    0x00FF00
                } else {
                    val discordIDInspector = walletDB.getInspectorWallet(walletId) ?: return
                    val mentionInspector = discordBot?.getMentionUser(discordIDInspector)
                    val verificationDate = walletDB.getVerificationWalletDate(walletId)
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.notify".localized(), "localisation.discord.embed.verification.field.notif.reviewed".localized(), false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.reviewed.field.inspector".localized(), mentionInspector, false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.reviewed.field.target".localized(), "$mentionTarget (MC: $playerNameTarget)", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.wallet".localized(), "$walletId", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.reviewed.field.date".localized(), "`$verificationDate`", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.status".localized(), status, false))
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
                    functions.sendMessageIsPlayerOnline(walletDB.getUUIDbyWalletID(walletId).toString(), status)
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.status".localized(), status, false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.inspector.reject".localized(), event.user.asMention, false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.target.reject".localized(), "$mentionTarget (MC: $playerNameTarget)", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.wallet".localized(), "$walletId", false))
                    0xFF0000
                } else {
                    val discordIDInspector = walletDB.getInspectorWallet(walletId) ?: return
                    val mentionInspector = discordBot?.getMentionUser(discordIDInspector)
                    val verificationDate = walletDB.getVerificationWalletDate(walletId)
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.notify".localized(),"localisation.discord.embed.verification.field.notif.reviewed".localized(), false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.reviewed.field.inspector".localized(), mentionInspector, false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.reviewed.field.target".localized(), "$mentionTarget (MC: $playerNameTarget)", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.wallet".localized(), "$walletId", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.reviewed.field.date".localized(), "`$verificationDate`", false))
                    embedFields.add(MessageEmbed.Field("localisation.discord.embed.verification.field.status".localized(), status, false))
                    0x808080
                }
            }
            else -> 0x808080
        }

        val newEmbed = discordNotifier.createEmbedMessage(
            title = "localisation.discord.embed.title.verification".localized(),
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
