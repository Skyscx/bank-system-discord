package gui.accountmenu.renamingaccount

import data.Database
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent


class RenamingAccountInventoryEvent(private val database: Database, private val renamingAccountInventory: RenamingAccountInventory) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (renamingAccountInventory.isInitialized() && event.inventory == renamingAccountInventory.getRenameInventory()) {
            event.isCancelled = true
            val player = event.whoClicked as Player
            val clickedItem = event.currentItem ?: return
            val clickedSlot = event.slot

            when (clickedSlot) {
                in listOf(0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 20, 21, 22, 23, 24, 27, 28, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 42, 45, 46, 47, 48, 49, 50, 51) -> {
                    renamingAccountInventory.setCurrentWalletName(
                        (renamingAccountInventory.getCurrentWalletName() + clickedItem.itemMeta?.displayName) ?: ""
                    )
                    renamingAccountInventory.updateWalletName()
                }
                5, 14 -> {
                    renamingAccountInventory.setCurrentWalletName(
                        (renamingAccountInventory.getCurrentWalletName() + clickedItem.itemMeta?.displayName) ?: ""
                    )
                    renamingAccountInventory.updateWalletName()
                }
                18, 19 -> {
                    renamingAccountInventory.setCurrentWalletName(renamingAccountInventory.getCurrentWalletName() + player.name)
                    renamingAccountInventory.updateWalletName()
                }
                25, 26 -> {
                    if (renamingAccountInventory.getCurrentWalletName().isNotEmpty()) {
                        renamingAccountInventory.setCurrentWalletName(renamingAccountInventory.getCurrentWalletName().substring(0, renamingAccountInventory.getCurrentWalletName().length - 1))
                        renamingAccountInventory.updateWalletName()
                    }
                }
                34, 35 -> {
                    player.closeInventory()
                }
                43, 44 -> {
                    if (renamingAccountInventory.getCurrentWalletName().length in 5..32) {
                        database.setNameWalletByIDWallet(renamingAccountInventory.getCurrentWalletName(), renamingAccountInventory.getWalletID())
                        player.closeInventory()
                    } else {
                        player.sendMessage("Wallet name must be between 5 and 32 characters long.")
                    }
                }
                52, 53 -> {
                    player.sendMessage("Guide link: https://example.com/guide")
                }
            }
        } else {
            println("Inventory is not initialized or does not match: ${event.inventory == renamingAccountInventory.getRenameInventory()}, ${renamingAccountInventory.isInitialized()}")
        }
    }

}