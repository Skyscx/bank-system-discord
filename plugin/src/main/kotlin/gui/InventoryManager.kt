package gui

import gui.сonfirmations.OpenAccountInventory
import org.bukkit.entity.Player

class InventoryManager {
    private val inventoryCreators = mapOf(
        "open" to OpenAccountInventory(),
        /**more inventory**/
    )

    fun openInventory(player: Player, inventoryType: String) {
        val creator = inventoryCreators[inventoryType]
        if (creator != null) {
            val inventory = creator.createInventory(player)
            player.openInventory(inventory)
        } else {
            player.sendMessage("Unknown inventory type: $inventoryType")
        }
    }
}