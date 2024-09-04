package gui.wallletmenu.reportwallet

import App.Companion.instance
import functions.Functions
import gui.InventoryManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletReportInventoryEvent : Listener {
    private val functions = Functions()
    private val inventoryManager = InventoryManager()
    private val playersWaitingForMessage = mutableMapOf<Player, String>()

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.CHEST) {
            val title = e.view.title()
            val expectedTitle = "Решение проблемы"
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return

                    val typeMap = mapOf(
                        "Выбор проблемы" to "OTHER",
                        "Ошибка данных" to "DATA",
                        "Не работает" to "WORK",
                        "Медленная загрузка" to "PING",
                        "Другое" to "OTHER"
                    )

                    val displayNameText = PlainTextComponentSerializer.plainText().serialize(displayNameComponent)
                    val type = typeMap[displayNameText] ?: return
                    playersWaitingForMessage[player] = type
                    player.sendMessage("Пожалуйста, введите ваше сообщение в чат.")
                }
            }
        }
    }

    @EventHandler
    fun onPlayerChat(e: AsyncChatEvent) {
        val player = e.player
        if (playersWaitingForMessage.containsKey(player)) {
            e.isCancelled = true // Отменяем отправку сообщения в чат
            val type = playersWaitingForMessage.remove(player) ?: return
            val message = PlainTextComponentSerializer.plainText().serialize(e.message())
            val command = "wallet report $type $message"
            Bukkit.getScheduler().runTask(instance, Runnable {
                player.performCommand(command)
            })
        }
    }
}