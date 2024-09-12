package bank.commands.wallets.collection

//import functions.CooldownManager
import App.Companion.configPlugin
import App.Companion.reportsDB
import App.Companion.userDB
import discord.dsbot.DiscordNotifier
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player


class ReportCommandHandler(config: FileConfiguration) {
    private val discordNotifier = DiscordNotifier(config)
    //private val cooldownManager = CooldownManager()
    //todo: Подумать над кулдауном!!!
    fun handleReportCommand(sender: CommandSender, args: Array<String>) {
        if (args.size < 3) {
            sender.sendMessage("Используйте: /wallet report [TYPE] [MESSAGE]")
            return
        }

//        val timeLeft: java.time.Duration = cooldownManager.getRemainingCooldown(sender.uniqueId)
//        if (timeLeft.isZero || timeLeft.isNegative) {return}

        val senderName = sender.name
        val player = sender as Player
        val senderUUID = player.uniqueId
        val senderDID = userDB.getDiscordIDbyUUID(senderUUID.toString()) ?: "NULL"

        val type = args[1]
        val message = args.drop(2).joinToString(" ")

        val reason = when (type.uppercase()) {
            "DATA" -> "Ошибка данных"
            "WORK" -> "Не работает"
            "PING" -> "Медленная загрузка"
            "OTHER" -> "Другое"
            else -> {
                sender.sendMessage("Неверный тип жалобы. Используйте DATA, WORK, PING или OTHER.")
                return
            }
        }
        val reportID = reportsDB.generateUniqueReportID()
        val channelIdTarget = configPlugin.getString("channel-id-reports") ?: return
        val buttons = createButtons(reportID)

        discordNotifier.sendEmbedMessageAndButtons(
            channelId = channelIdTarget,
            title = "Жалоба от игрока",
            description = "Игрок $senderName жалуется",
            color = 1,
            embedType = EmbedType.RICH,
            buttons = buttons,
            fields = listOf(
                MessageEmbed.Field("Причина жалобы", reason, false),
                MessageEmbed.Field("Жалоба", message, false),
                MessageEmbed.Field("Идентификатор", reportID.toString(), false)
            )
        )
        reportsDB.insertBankReport(
            senderName,
            senderUUID.toString(),
            senderDID,
            reason,
            message,
            "MC",
            reportID
        )
        sender.sendMessage("Жалоба отправлена!")
        // Устанавливаем кулдаун на 1 час
        //cooldownManager.setCooldown(sender.uniqueId, java.time.Duration.ofSeconds(CooldownManager.DEFAULT_COOLDOWN))
    }

    private fun createButtons(reportID: Int): List<Button> {
        return listOf(
            Button.of(ButtonStyle.SUCCESS, "reportWalletApprove:$reportID", "Одобрить"),
            Button.of(ButtonStyle.DANGER, "reportWalletReject:$reportID", "Отклонить"),
            Button.of(ButtonStyle.PRIMARY, "reportWalletContact:$reportID", "Связаться"),
        )
    }
}
