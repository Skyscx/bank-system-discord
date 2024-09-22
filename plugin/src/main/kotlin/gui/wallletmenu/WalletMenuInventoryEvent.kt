package gui.wallletmenu

import App.Companion.configPlugin
import App.Companion.localized
import functions.Functions
import gui.InventoryManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletMenuInventoryEvent : Listener {
    private val functions = Functions()
    private val inventoryManager = InventoryManager()

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.CHEST) {
            val title = e.view.title()
            val expectedTitle = "localisation.inventory.title.menu-wallet".localized()
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return

                    val actionMap: Map<String, () -> Unit> = mapOf(
                        "localisation.inventory.item.open-wallet".localized() to { inventoryManager.openInventory(player, "open") },
                        "localisation.inventory.item.close-wallet".localized() to { inventoryManager.openInventory(player, "close") },
                        "localisation.inventory.item.guid-book".localized() to {
                            val guidLink = configPlugin.getString("guid-link") ?: "Missing URL"
                            functions.sendMessagePlayer(player, "localisation.messages.out.guid-link".localized())
                            functions.sendClickableLink(player, guidLink, guidLink)
                        },
                        "localisation.inventory.item.actions".localized() to { inventoryManager.openInventory(player, "actions") },
                        "localisation.inventory.item.report".localized() to { inventoryManager.openInventory(player, "reports") },
                        "localisation.inventory.item.transfer".localized() to { inventoryManager.openInitialTransferInventory(player) },
                        "localisation.inventory.item.history".localized() to { inventoryManager.openInitialHistoryInventory(player) },
                        "localisation.inventory.item.convert".localized() to { inventoryManager.openInventory(player, "convert") }
                    )

                    actionMap[displayNameComponent.toString()]?.invoke()
                }
            }
        }
    }
}