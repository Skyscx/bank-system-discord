package gui.wallletmenu.closewallet

import App.Companion.localized
import App.Companion.userDB
import functions.Functions
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletCloseInventoryEvent(config: FileConfiguration) : Listener{
    private val functions = Functions()
   //private val countAccountConfig = config.getInt("count-free-accounts") TODO: Вернуть в будущем когда будет система разных кошельков.


    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.HOPPER) {
            val title = e.view.title()
            val expectedTitle = "localisation.inventory.title.wallet-close-confirmation".localized()
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleAccept = "localisation.inventory.item.accept".localized()
                    if (functions.isComponentEqual(displayNameComponent, titleAccept)) {
                        val uuid = player.uniqueId.toString()
                        val walletID = userDB.getDefaultWalletByUUID(uuid) ?: return
                        player.performCommand("wallet remove $walletID true")
                    }
                    return
                }
            }
        }
    }
}