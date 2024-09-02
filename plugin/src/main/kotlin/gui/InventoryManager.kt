package gui

import gui.wallletmenu.WalletMenuInventory
import gui.wallletmenu.openwallet.WalletOpenInventory
import gui.wallletmenu.removewallet.WalletRemoveInventory
import org.bukkit.entity.Player

class InventoryManager {
    private val inventoryCreators = mapOf(
        "open" to WalletOpenInventory(),
        "remove" to WalletRemoveInventory(),
        "menu" to WalletMenuInventory()
        /**more inventory**/
    )

    fun openInventory(player: Player, inventoryType: String) {
        val creator = inventoryCreators[inventoryType]
        if (creator != null) {
            val inventory = creator.createInventory(player)
            player.openInventory(inventory)
        } else {
            player.sendMessage("Unknown inventory type: $inventoryType") //todo: переделать сообщение на конфиг месседж
        }
    }
}