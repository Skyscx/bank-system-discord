package gui.wallletmenu.transferwallet

import App.Companion.localizationManager
import data.TransferDataManager
import gui.InventoryCreator
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class ConfirmTransferInventory(private val transferDataManager: TransferDataManager) : InventoryCreator {

    override fun createInventory(player: Player): Inventory {
        val transferData = transferDataManager.getTransferData(player) ?: return Bukkit.createInventory(null, 54, Component.text("Error"))
        val targetPlayerName = transferData.targetPlayerName
        val amount = transferData.amount

        val inventory = Bukkit.createInventory(null, 54, Component.text(localizationManager.getMessage("localisation.inventory.item.confirm-transfer")))

        // Добавьте предмет с названием выбранной головы в центр
        val centerItem = createCenterItem(targetPlayerName, amount)
        inventory.setItem(22, centerItem)

        // Добавьте кнопки подтверждения и отмены
        val confirmItem = createConfirmItem()
        val cancelItem = createCancelItem()
        inventory.setItem(20, confirmItem)
        inventory.setItem(24, cancelItem)

        return inventory
    }

    private fun createCenterItem(targetPlayerName: String, amount: Int): ItemStack {

        return ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta
            meta.displayName(Component.text(localizationManager.getMessage("localisation.inventory.item.transfer-info", "amount" to amount.toString(), "player" to targetPlayerName)))
            itemMeta = meta
        }
    }

    private fun createConfirmItem(): ItemStack {
        return ItemStack(Material.GREEN_WOOL).apply {
            val meta = itemMeta
            meta?.displayName(Component.text(localizationManager.getMessage("localisation.inventory.item.accept")))
            itemMeta = meta
        }
    }

    private fun createCancelItem(): ItemStack {
        return ItemStack(Material.RED_WOOL).apply {
            val meta = itemMeta
            meta?.displayName(Component.text(localizationManager.getMessage("localisation.inventory.item.reject")))
            itemMeta = meta
        }
    }
}