package bank.commands.wallets.collection

import App.Companion.historyDB
import App.Companion.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.concurrent.CompletableFuture

class HistoryCommandHandler {
    fun handleHistoryCommand(sender: CommandSender, args: Array<String>) {
        val player = sender as Player
        val page = if (args.size > 1) args[1].toIntOrNull() ?: 1 else 1
        showHistory(player, page)
        return
    }
    //todo: После первого релиза с нормальными сообщениями, перенести сообщения в конфиг.
    private fun showHistory(player: Player, page: Int = 1): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val pageSize = 7
        val offset = (page - 1) * pageSize
        val userUUID = player.uniqueId.toString()

        Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable {
            try {
                val result = historyDB.getUserHistory(userUUID, pageSize, offset)

                if (result.isEmpty()) {
                    player.sendMessage("У вас нет операций.")
                    future.complete(false)
                    return@Runnable
                }

                player.sendMessage("История операций (Страница $page):")

                for (row in result) {
                    val senderUUID = row["SenderUUID"] as String
                    val amount = row["Amount"] as Int
                    val currency = row["Currency"] as String
                    val senderName = row["SenderName"] as String
                    val targetName = row["TargetName"] as String
                    val dateStr = row["Date"] as String

                    val date = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").parse(dateStr)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                    val formattedDate = dateFormat.format(date)

                    val icon = if (senderUUID == userUUID) "📤" else "📥"
                    val description = if (senderUUID == userUUID) {
                        "Отправлено $amount $currency пользователю $targetName"
                    } else {
                        "Получено $amount $currency от пользователя $senderName"
                    }

                    player.sendMessage("$icon $formattedDate - $description")
                }
                val message = Component.text()
                if (page > 1) {
                    val prevPage = Component.text("[Предыдущая страница]")
                        .color(TextColor.color(0x00FF00)) // Зеленый цвет
                        .decoration(TextDecoration.UNDERLINED, true)
                        .clickEvent(ClickEvent.runCommand("/wallet history ${page - 1}"))
                        .hoverEvent(HoverEvent.showText(Component.text("Кликните для перехода на предыдущую страницу")))
                    message.append(prevPage)
                }
                if (result.size == pageSize) {
                    if (page > 1) {
                        message.append(Component.text(" | "))
                    }
                    val nextPage = Component.text("[Следующая страница]")
                        .color(TextColor.color(0x00FF00)) // Зеленый цвет
                        .decoration(TextDecoration.UNDERLINED, true)
                        .clickEvent(ClickEvent.runCommand("/wallet history ${page + 1}"))
                        .hoverEvent(HoverEvent.showText(Component.text("Кликните для перехода на следующую страницу")))
                    message.append(nextPage)
                }

                player.sendMessage(message)

                future.complete(true)
            } catch (e: SQLException) {
                e.printStackTrace()
                future.complete(false)
            }
        })

        return future
    }
}