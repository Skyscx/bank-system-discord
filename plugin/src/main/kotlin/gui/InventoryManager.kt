package gui

import gui.accountmenu.openaccount.AccountOpenInventory
import gui.accountmenu.removeaccount.AccountRemoveInventory
import org.bukkit.entity.Player

class InventoryManager {
    private val inventoryCreators = mapOf(
        "open" to AccountOpenInventory(),
        "remove" to AccountRemoveInventory()
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