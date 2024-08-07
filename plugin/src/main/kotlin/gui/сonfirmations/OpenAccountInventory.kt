package gui.сonfirmations

import gui.InventoryCreator
import gui.SystemGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class OpenAccountInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {
        val openAccount = Bukkit.createInventory(null, InventoryType.HOPPER, "Подтверждение операции")
        val accept = systemGUI.createItem(Material.GREEN_WOOL, "Подтвердить!", listOf("Подтвердив операцию, с вашего инвентаря заберутся N алмазной руды."))
        openAccount.setItem(1, accept)
        val close = systemGUI.createItem(Material.RED_WOOL, "Отклонить!")
        openAccount.setItem(3, close)
        return openAccount
    }
}