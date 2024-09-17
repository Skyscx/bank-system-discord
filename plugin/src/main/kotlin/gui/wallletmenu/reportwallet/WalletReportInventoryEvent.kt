package gui.wallletmenu.reportwallet

import App.Companion.instance
import functions.Functions
import gui.InventoryManager
import gui.SystemGUI
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletReportInventoryEvent : Listener {
    private val functions = Functions()
    private val inventoryManager = InventoryManager()
    private val playersWaitingForMessage = mutableMapOf<Player, String>()
    private val systemGUI = SystemGUI()


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
                        "Ошибка данных" to "DATA",
                        "Не работает" to "WORK",
                        "Медленная загрузка" to "PING",
                        "Другое" to "OTHER"
                    )

                    val displayNameText = PlainTextComponentSerializer.plainText().serialize(displayNameComponent)
                    val type = typeMap[displayNameText] ?: return
                    playersWaitingForMessage[player] = type
                    openAnvilGUI(player)
                    return
                }
            }
        }
    }

//    @EventHandler
//    fun onPlayerChat(e: AsyncChatEvent) {
//        val player = e.player
//        if (playersWaitingForMessage.containsKey(player)) {
//            e.isCancelled = true // Отменяем отправку сообщения в чат
//            val type = playersWaitingForMessage.remove(player) ?: return
//            val message = PlainTextComponentSerializer.plainText().serialize(e.message())
//            val command = "wallet report $type $message"
//            Bukkit.getScheduler().runTask(instance, Runnable {
//                player.performCommand(command)
//            })
//        }
//    }

    private fun openAnvilGUI(player: Player) {
        val item = systemGUI.createItem(
            Material.PAPER,
            name = "report",
            customModelData = 2

        )
        AnvilGUI.Builder()
            .onClick { slot, stateSnapshot ->
                if (slot == AnvilGUI.Slot.OUTPUT) {
                    val text = stateSnapshot.text
                    val type = playersWaitingForMessage.remove(player)
                    val command = "wallet report $type $text"
                    Bukkit.getScheduler().runTask(instance, Runnable {
                        player.performCommand(command)
                    })



                    return@onClick listOf(AnvilGUI.ResponseAction.close())
                }
                return@onClick emptyList()
            }
            .text(" ")
            .itemLeft(item)
            .title("report")
            .plugin(instance)
            .open(player)

    }
}