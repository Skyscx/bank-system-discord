package gui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

interface InventoryCreator {
    fun createInventory(player: Player): Inventory
}