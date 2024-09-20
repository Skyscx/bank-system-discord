package bank.commands.wallets.collection

import App.Companion.configPlugin
import App.Companion.localized
import App.Companion.reportsDB
import App.Companion.userDB
import discord.dsbot.DiscordNotifier
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player


class ReportCommandHandler(config: FileConfiguration) {
    private val discordNotifier = DiscordNotifier(config)

    fun handleReportCommand(player: Player, args: Array<String>) {
        if (args.size < 3) {
            player.sendMessage("localisation.messages.usage.account.report".localized())
            return
        }
        val playerUUID = player.uniqueId
        userDB.isPlayerExists(playerUUID).thenAccept { exists ->
            if (exists) {
                val senderName = player.name
                val senderUUID = playerUUID.toString()
                val senderDID = userDB.getDiscordIDbyUUID(senderUUID) ?: "NULL"

                val type = args[1]
                val message = args.drop(2).joinToString(" ")

                val reason = when (type.uppercase()) {
                    "DATA" -> "localisation.report.type.data".localized()
                    "WORK" -> "localisation.report.type.work".localized()
                    "PING" -> "localisation.report.type.ping".localized()
                    "OTHER" -> "localisation.report.type.other".localized()
                    else -> {
                        player.sendMessage("localisation.report.type.not".localized())
                        return@thenAccept
                    }
                }
                val reportID = reportsDB.generateUniqueReportID()
                val channelIdTarget = configPlugin.getString("channel-id-reports") ?: return@thenAccept
                val buttons = createButtons(reportID)

                discordNotifier.sendEmbedMessageAndButtons(
                    channelId = channelIdTarget,
                    title = "localisation.discord.embed.report.title.report".localized(),
                    description = "localisation.discord.embed.report.description.report".localized("senderName" to senderName),
                    color = 1,
                    embedType = EmbedType.RICH,
                    buttons = buttons,
                    fields = listOf(
                        MessageEmbed.Field("localisation.discord.embed.report.field.reason".localized(), reason, false),
                        MessageEmbed.Field("localisation.discord.embed.report.field.message".localized(), message, false),
                        MessageEmbed.Field("localisation.discord.embed.report.field.reportID".localized(), reportID.toString(), false)
                    )
                )
                reportsDB.insertBankReport(
                    senderName,
                    senderUUID,
                    senderDID,
                    reason,
                    message,
                    "MC",
                    reportID
                )
                player.sendMessage("localisation.messages.out.wallet.report.send".localized())
            } else {
                player.sendMessage("localisation.error.not-search-target".localized())
            }
        }.exceptionally { e ->
            e.printStackTrace()
            player.sendMessage("Произошла ошибка при проверке существования игрока.")
            null
        }
    }

    private fun createButtons(reportID: Int): List<Button> {
        return listOf(
            Button.of(ButtonStyle.SUCCESS, "reportWalletApprove:$reportID", "localisation.discord.embed.report.buttons.approve".localized()),
            Button.of(ButtonStyle.DANGER, "reportWalletReject:$reportID", "localisation.discord.embed.report.buttons.reject".localized()),
            Button.of(ButtonStyle.PRIMARY, "reportWalletContact:$reportID", "localisation.discord.embed.report.buttons.contact".localized()),
        )
    }
}
