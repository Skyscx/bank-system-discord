package gui.wallletmenu.reportwallet

import App.Companion.instance
import App.Companion.localized
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
            val expectedTitle = "localisation.inventory.title.report-menu".localized()
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val typeMap = mapOf(
                        "localisation.report.type.data".localized() to "DATA",
                        "localisation.report.type.work".localized() to "WORK",
                        "localisation.report.type.ping".localized() to "PING",
                        "localisation.report.type.other".localized() to "OTHER"
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
            .text("localisation.inventory.anvil.report.item.text".localized())
            .itemLeft(item)
            .title("localisation.inventory.anvil.report.title".localized())
            .plugin(instance)
            .open(player)

    }
}