package bank.commands.wallets.collection

import App.Companion.configPlugin
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class ReportCommandHandler(config: FileConfiguration) {
    private val jda: JDA = DiscordBot.getJDA() ?: throw IllegalStateException("JDA is not initialized")
    private val discordNotifier = DiscordNotifier(config)


    fun handleReportCommand(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage("Эту команду может использовать только игрок.")
            return
        }

        if (args.size < 3) {
            sender.sendMessage("Используйте: /wallet report [TYPE] [MESSAGE]")
            return
        }

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
        val channelIdTarget = configPlugin.getString("channel-id-reports") ?: return
        val buttons = createButtons()

        discordNotifier.sendEmbedMessageAndButtons(
            channelId = channelIdTarget,
            title = "Жалоба от игрока",
            description = "Игрок ${sender.name} жалуется",
            color = 1,
            embedType = EmbedType.RICH,
            buttons = buttons,
            fields = listOf(
                MessageEmbed.Field("Причина жалобы", reason, false),
                MessageEmbed.Field("Жалоба", message, false)
            )
        )

//        val embed = createEmbed(sender.name, reason, message)
//        val channel = jda.getTextChannelById(channelIdTarget)
//        channel?.sendMessage(MessageCreateBuilder().setEmbeds(embed).setActionRow(buttons).build())?.queue()
//
    }

//    private fun createEmbed(playerName: String, reason: String, message: String): MessageEmbed {
//        return MessageEmbed(
//            null, // url
//            "Жалоба от игрока", // title
//            "Игрок $playerName жалуется.", // description
//            EmbedType.RICH, // type
//            null, // timestamp
//            1, // color
//            null, // thumbnail
//            null, // siteProvider
//            null, // author
//            null, // videoInfo
//            null, // footer
//            null, // image
//            listOf(
//                MessageEmbed.Field("Причина жалобы", reason, false),
//                MessageEmbed.Field("Жалоба", message, false)
//            ) // fields
//        )
//    }

    private fun createButtons(): List<Button> {
        return listOf(
            Button.success("reportWalletApprove", "Одобрить"),
            Button.danger("reportWalletReject", "Отклонить"),
            Button.primary("reportWalletContact", "Связаться")
        )
    }
}
