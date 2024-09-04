package gui

import gui.wallletmenu.WalletMenuInventory
import gui.wallletmenu.actionwallet.WalletActionsInventory
import gui.wallletmenu.closewallet.WalletCloseInventory
import gui.wallletmenu.openwallet.WalletOpenInventory
import gui.wallletmenu.reportwallet.WalletReportInventory
import oldnotusagefiles.removewallet.WalletRemoveInventory
import org.bukkit.entity.Player

class InventoryManager {
    private val inventoryCreators = mapOf(
        "open" to WalletOpenInventory(),
        "remove" to WalletRemoveInventory(), //todo: remove
        "menu" to WalletMenuInventory(),
        "close" to WalletCloseInventory(),
        "actions" to WalletActionsInventory(),
        "reports" to WalletReportInventory()
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