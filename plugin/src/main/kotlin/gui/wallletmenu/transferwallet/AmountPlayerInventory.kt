package gui.wallletmenu.transferwallet

import App.Companion.localized
import data.managers.TransferDataManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class AmountPlayerInventory(private val transferDataManager: TransferDataManager) : InventoryCreator {
    private val systemGUI = SystemGUI()

    override fun createInventory(player: Player): Inventory {
        val transferData = transferDataManager.getTransferData(player) ?: return Bukkit.createInventory(null, 54, Component.text(
            "localisation.error".localized()))
        val targetPlayerName = transferData.targetPlayerName

        val inventory = Bukkit.createInventory(null, 54, Component.text("localisation.inventory.title.select-amount-transfer".localized()))

        val add1 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+1",
            listOf("localisation.inventory.lore.item.add-amount.transfer-menu".localized()),
            1
        )
        val add16 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+16",
            listOf("localisation.inventory.lore.item.add-amount.transfer-menu".localized()),
            1
        )
        val add64 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+64",
            listOf("localisation.inventory.lore.item.add-amount.transfer-menu".localized()),
            1
        )
        val get1 = systemGUI.createItem(
            Material.RED_WOOL,
            "§4-1",
            listOf("localisation.inventory.lore.item.get-amount.transfer-menu".localized()),
            1
        )
        val get16 = systemGUI.createItem(
            Material.RED_WOOL,
            "§4-16",
            listOf("localisation.inventory.lore.item.get-amount.transfer-menu".localized()),
            1
        )
        val get64 = systemGUI.createItem(
            Material.RED_WOOL,
            "§4-64",
            listOf("localisation.inventory.lore.item.get-amount.transfer-menu".localized()),
            1
        )

        inventory.setItem(10, add1)
        inventory.setItem(11, add16)
        inventory.setItem(12, add64)
        inventory.setItem(14, get1)
        inventory.setItem(15, get16)
        inventory.setItem(16, get64)

        val centerItem = createCenterItem(targetPlayerName, transferData.amount)
        inventory.setItem(22, centerItem)

        val confirmAmountItem = systemGUI.createItem(
            Material.GREEN_WOOL,
            "localisation.inventory.item.accept".localized(),
            customModelData = 4
        )
        inventory.setItem(31, confirmAmountItem)

        return inventory
    }

    fun updateItem(player: Player, inventory: Inventory) {
        val transferData = transferDataManager.getTransferData(player) ?: return
        val targetPlayerName = transferData.targetPlayerName

        val centerItem = createCenterItem(targetPlayerName, transferData.amount)
        inventory.setItem(22, centerItem)
    }

    private fun createCenterItem(targetPlayerName: String, amount: Int): ItemStack {
        return systemGUI.createItem(
            Material.PAPER,
            targetPlayerName,
            listOf("localisation.inventory.lore.item.select-amount.transfer-menu".localized( "amount" to amount.toString())),
            3
        )
    }
}