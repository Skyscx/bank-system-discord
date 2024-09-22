package gui.wallletmenu.transferwallet

import App.Companion.localized
import data.TransferDataManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

//todo : продолжить 20.09.2024
class ConfirmTransferInventory(private val transferDataManager: TransferDataManager) : InventoryCreator {
    private val systemGUI = SystemGUI()

    override fun createInventory(player: Player): Inventory {
        val transferData = transferDataManager.getTransferData(player) ?: return Bukkit.createInventory(null, 54, Component.text(
            "localisation.error".localized()))
        val targetPlayerName = transferData.targetPlayerName
        val amount = transferData.amount
        val comment = transferData.comment ?: ""

        val inventory = Bukkit.createInventory(null, 54, Component.text("localisation.inventory.title.confirm-transfer".localized()))

        val centerItem = createCenterItem(targetPlayerName, amount, comment)
        inventory.setItem(22, centerItem)

        val confirmItem = systemGUI.createItem(
            Material.GREEN_WOOL,
            "localisation.inventory.item.accept".localized(),
            customModelData = 2
        )
        val cancelItem = systemGUI.createItem(
            Material.RED_WOOL,
            "localisation.inventory.item.reject".localized(),
            customModelData = 2
        )

        inventory.setItem(20, confirmItem)
        inventory.setItem(24, cancelItem)

        return inventory
    }

    private fun createCenterItem(targetPlayerName: String, amount: Int, comment: String): ItemStack {
        return systemGUI.createItem(
            Material.PAPER,
            targetPlayerName,
            listOf(
                "localisation.inventory.lore.item.confirm-head.transfer-menu".localized(
                    "amount" to amount.toString(),
                    "comment" to comment
                )
            ),
            3
        )
    }
}