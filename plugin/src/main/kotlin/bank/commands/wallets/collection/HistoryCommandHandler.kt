package bank.commands.wallets.collection

import App.Companion.historyDB
import App.Companion.instance
import App.Companion.localized
import App.Companion.userDB
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.concurrent.CompletableFuture

class HistoryCommandHandler {
    fun handleHistoryCommand(player: Player, args: Array<String>) {
        val page = if (args.size > 1) args[1].toIntOrNull() ?: 1 else 1
        val playerUUID = player.uniqueId

        userDB.isPlayerExists(playerUUID).thenAccept { exists ->
            if (exists) {
                showHistory(player, page, playerUUID.toString())
            } else {
                player.sendMessage("localisation.error.not-search-target".localized())
            }
        }.exceptionally { e ->
            e.printStackTrace()
            null
        }
    }

    private fun showHistory(player: Player, page: Int = 1, userUUID: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val pageSize = 7
        val offset = (page - 1) * pageSize
        Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable {
            try {
                val result = historyDB.getUserHistory(userUUID, pageSize, offset)

                if (result.isEmpty()) {
                    player.sendMessage("localisation.messages.out.wallet.history.empty".localized())
                    future.complete(false)
                    return@Runnable
                }

                player.sendMessage("localisation.messages.out.wallet.history.page".localized("page" to page.toString()))

                for (row in result) {
                    val senderUUID = row["SenderUUID"] as String
                    val amount = row["Amount"] as Int
                    val currency = row["Currency"] as String
                    val senderName = row["SenderName"] as String
                    val targetName = row["TargetName"] as String
                    val dateStr = row["Date"] as String
                    val typeOperation = row["TypeOperation"] as String

                    val date = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").parse(dateStr)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                    val formattedDate = dateFormat.format(date)

                    val icon = when (typeOperation) {
                        "TRANSFER" -> if (senderUUID == userUUID) "📤" else "📥"
                        "OPEN_WALLET" -> "🔓"
                        "ATTEMPT_OPEN_WALLET" -> "🔒"
                        "RENAMING" -> "📝"
                        "RENAMING_PLAYER" -> "📝"
                        "CLOSE_WALLET" -> "🔒"
                        "ADD_BALANCE" -> "➡"
                        "GET_BALANCE" -> "⬅"
                        else -> "❓"
                    }

                    val description = when (typeOperation) {
                        "TRANSFER" -> if (senderUUID == userUUID) {
                            "localisation.messages.out.wallet.history.transfer.send".localized(
                                "amount" to amount.toString(),
                                "currency" to currency,
                                "targetName" to targetName
                            )
                        } else {
                            "localisation.messages.out.wallet.history.transfer.take".localized(
                                "amount" to amount.toString(),
                                "currency" to currency,
                                "senderName" to senderName
                            )
                        }
                        "OPEN_WALLET" -> "localisation.messages.out.wallet.history.open-wallet".localized("senderName" to senderName)
                        "ATTEMPT_OPEN_WALLET" -> "localisation.messages.out.wallet.history.attempt-open-wallet".localized("senderName" to senderName)
                        "RENAMING" -> "localisation.messages.out.wallet.history.renaming".localized("senderName" to senderName)
                        "CLOSE_WALLET" -> "localisation.messages.out.wallet.history.close-wallet".localized("senderName" to senderName)
                        "ADD_BALANCE" -> "localisation.messages.out.wallet.history.add-balance".localized("amount" to amount.toString())
                        "GET_BALANCE" -> "localisation.messages.out.wallet.history.get-balance".localized("amount" to amount.toString())
                        else -> "localisation.messages.out.wallet.history.other".localized()
                    }

                    player.sendMessage("$icon $formattedDate - $description")
                }

                val message = Component.text()
                if (page > 1) {
                    val prevPage = Component.text("localisation.previous-page".localized())
                        .color(TextColor.color(0x00FF00)) // Зеленый цвет
                        .decoration(TextDecoration.UNDERLINED, true)
                        .clickEvent(ClickEvent.runCommand("/wallet history ${page - 1}"))
                        .hoverEvent(HoverEvent.showText(Component.text("localisation.messages.out.hover.click-previous-page".localized())))
                    message.append(prevPage)
                }
                if (result.size == pageSize) {
                    if (page > 1) {
                        message.append(Component.text(" | "))
                    }
                    val nextPage = Component.text("localisation.next-page".localized())
                        .color(TextColor.color(0x00FF00)) // Зеленый цвет
                        .decoration(TextDecoration.UNDERLINED, true)
                        .clickEvent(ClickEvent.runCommand("/wallet history ${page + 1}"))
                        .hoverEvent(HoverEvent.showText(Component.text("localisation.messages.out.hover.click-next-page".localized())))
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