package gui.сonfirmations

import gui.SystemGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class OpenAccountInventory {
    private val systemGUI = SystemGUI()
    fun openAccountMenu(player: Player) {
        val openAccount = Bukkit.createInventory(null, InventoryType.HOPPER, "Подтверждение операции")
        // Accept
        val accept = systemGUI.createItem(Material.GREEN_WOOL, "Подтвердить!", listOf("Подтвердив операцию, с вашего инвентаря заберутся N алмазной руды."))
        openAccount.setItem(1, accept)
        // CloseMenu
        val close = systemGUI.createItem(Material.RED_WOOL, "Отклонить!")
        openAccount.setItem(3, close)

        player.openInventory(openAccount)
    }
}