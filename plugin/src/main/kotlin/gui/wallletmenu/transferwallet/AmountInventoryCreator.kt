package gui.wallletmenu.transferwallet

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class AmountInventoryCreator {

    fun createAmountInventory(targetPlayerName: String): Inventory {
        val inventory = Bukkit.createInventory(null, 54, Component.text("Select Amount"))

        // Добавьте предметы для выбора суммы
        val amounts = listOf(1, 10, 100, 1000)
        for (i in amounts.indices) {
            val amountItem = createAmountItem(amounts[i])
            inventory.setItem(i, amountItem)
        }

        // Добавьте предмет с названием выбранной головы в центр
        val centerItem = createCenterItem(targetPlayerName)
        inventory.setItem(22, centerItem)

        return inventory
    }

    private fun createAmountItem(amount: Int): ItemStack {
        return ItemStack(Material.PAPER).apply {
            val meta = itemMeta
            meta?.displayName(Component.text("$amount").decoration(TextDecoration.BOLD, true))
            itemMeta = meta
        }
    }

    private fun createCenterItem(targetPlayerName: String): ItemStack {
        return ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta
            meta.displayName(Component.text(targetPlayerName).decoration(TextDecoration.BOLD, true))
            itemMeta = meta
        }
    }
}