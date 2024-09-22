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
                    e.isCancelled
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleOpenWallet = "localisation.inventory.item.open-wallet".localized()
                    val titleCloseWallet = "localisation.inventory.item.close-wallet".localized()
                    val titleGuidButton = "localisation.inventory.item.guid-book".localized()
                    val titleActionsWallet = "localisation.inventory.item.actions".localized()
                    val titleReportButton = "localisation.inventory.item.report".localized()
                    val titleTransferButton = "localisation.inventory.item.transfer".localized()
                    val titleHistoryButton = "localisation.inventory.item.history".localized()
                    if (functions.isComponentEqual(displayNameComponent, titleOpenWallet)) {
                        inventoryManager.openInventory(player, "open")
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleCloseWallet)) {
                        inventoryManager.openInventory(player, "close")
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleGuidButton)){
                        val guidLink = configPlugin.getString("guid-link") ?: "Missing URL"
                        functions.sendMessagePlayer(player, "localisation.messages.out.guid-link".localized())
                        functions.sendClickableLink(player, guidLink, guidLink)
                    }
                    if (functions.isComponentEqual(displayNameComponent,titleActionsWallet)){
                        inventoryManager.openInventory(player, "actions")
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleReportButton)){
                        inventoryManager.openInventory(player, "reports")
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleTransferButton)){
                        inventoryManager.openInitialTransferInventory(player)
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleHistoryButton)){
                        inventoryManager.openInitialHistoryInventory(player)
                    }
                    return
                }
            }
        }
    }

}