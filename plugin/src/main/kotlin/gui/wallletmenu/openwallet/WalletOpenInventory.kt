package gui.wallletmenu.openwallet

import App.Companion.localizationManager
import gui.InventoryCreator
import gui.SystemGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class WalletOpenInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {
        //todo: сделать получение цены создания кошелька.
        val amount = 0.toString() //todo: СЮДА

        val openAccount = Bukkit.createInventory(null, InventoryType.HOPPER, localizationManager.getMessage("localisation.account.open.confirmation.title"))
        val accept = systemGUI.createItem(Material.GREEN_WOOL, localizationManager.getMessage("localisation.inventory.item.accept"), listOf(localizationManager.getMessage("localisation.account.open.confirmation.item.accept.lore",
            "amount" to amount
        )))
        openAccount.setItem(1, accept)
        val close = systemGUI.createItem(Material.RED_WOOL, localizationManager.getMessage("localisation.inventory.item.reject"))
        openAccount.setItem(3, close)
        return openAccount
    }
}