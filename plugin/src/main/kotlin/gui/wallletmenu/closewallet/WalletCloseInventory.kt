package gui.wallletmenu.closewallet

import App.Companion.localizationManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class WalletCloseInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {
        //todo: сделать получение цены создания кошелька.

        val inventory = Bukkit.createInventory(null, InventoryType.HOPPER,
            Component.text(localizationManager.getMessage("localisation.inventory.title.wallet-close-confirmation")))
        val accept = systemGUI.createItem(
            Material.GREEN_WOOL,
            localizationManager.getMessage("localisation.inventory.item.accept"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.accept.close-wallet")),
            2
        )

        val close = systemGUI.createItem(
            Material.RED_WOOL,
            localizationManager.getMessage("localisation.inventory.item.reject"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.reject.close-wallet")),
            2)
        inventory.setItem(1, accept)
        inventory.setItem(3, close)
        return inventory
    }
}