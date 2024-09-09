package gui.wallletmenu.transferwallet

import App.Companion.localizationManager
import data.TransferDataManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class AmountPlayerInventory(private val transferDataManager: TransferDataManager) : InventoryCreator {
    private val systemGUI = SystemGUI()

    override fun createInventory(player: Player): Inventory {
        val transferData = transferDataManager.getTransferData(player) ?: return Bukkit.createInventory(null, 54, Component.text(
            localizationManager.getMessage("localisation.error")))
        val targetPlayerName = transferData.targetPlayerName

        val inventory = Bukkit.createInventory(null, 54, Component.text(localizationManager.getMessage("localisation.inventory.item.select-amount")))

        // Добавьте предметы для выбора суммы
        val add1 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+1",
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.add-amount.transfer-menu")),
            1
        )
        val add16 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+16",
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.add-amount.transfer-menu")),
            1
        )
        val add64 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+64",
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.add-amount.transfer-menu")),
            1
        )
        val get1 = systemGUI.createItem(
            Material.RED_WOOL,
            "§4-1",
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.get-amount.transfer-menu")),
            1
        )
        val get16 = systemGUI.createItem(
            Material.RED_WOOL,
            "§4-16",
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.get-amount.transfer-menu")),
            1
        )
        val get64 = systemGUI.createItem(
            Material.RED_WOOL,
            "§4-64",
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.get-amount.transfer-menu")),
            1
        )

        inventory.setItem(10, add1)
        inventory.setItem(11, add16)
        inventory.setItem(12, add64)
        inventory.setItem(14, get1)
        inventory.setItem(15, get16)
        inventory.setItem(16, get64)

        // Добавьте предмет с названием выбранной головы в центр
        val centerItem = createCenterItem(targetPlayerName, transferData.amount)
        inventory.setItem(22, centerItem)

        // Добавьте кнопку для подтверждения суммы
        val confirmAmountItem = createConfirmAmountItem()
        inventory.setItem(26, confirmAmountItem)

        return inventory
    }

    fun updateItem(player: Player, inventory: Inventory) {
        val transferData = transferDataManager.getTransferData(player) ?: return
        val targetPlayerName = transferData.targetPlayerName

        // Обновите предмет с названием выбранной головы в центре
        val centerItem = createCenterItem(targetPlayerName, transferData.amount)
        inventory.setItem(22, centerItem)
    }

    private fun createCenterItem(targetPlayerName: String, amount: Int): ItemStack {
        return ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta
            meta.displayName(Component.text(targetPlayerName).decoration(TextDecoration.BOLD, true))
            meta.lore(listOf(Component.text(localizationManager.getMessage("localisation.inventory.lore.item.select-amount.transfer-menu", "amount" to amount.toString())).decoration(TextDecoration.ITALIC, true)))
            itemMeta = meta
        }
    }

    private fun createConfirmAmountItem(): ItemStack {
        return ItemStack(Material.GREEN_WOOL).apply {
            val meta = itemMeta
            meta?.displayName(Component.text(localizationManager.getMessage("localisation.inventory.item.confirm-amount")).decoration(TextDecoration.BOLD, true))
            itemMeta = meta
        }
    }
}