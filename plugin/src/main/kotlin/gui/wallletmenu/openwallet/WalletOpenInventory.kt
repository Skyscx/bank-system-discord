package gui.wallletmenu.openwallet

import App.Companion.configPlugin
import App.Companion.localizationManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class WalletOpenInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    private val amount = configPlugin.getInt("price-account").toString()
    private val currency = configPlugin.getString("currency-block-default")

    override fun createInventory(player: Player): Inventory {
        //todo: сделать получение цены создания кошелька.

        val inventory = Bukkit.createInventory(null, InventoryType.HOPPER,
            Component.text(localizationManager.getMessage("localisation.inventory.title.wallet-open-confirmation")))
        val accept = systemGUI.createItem(
            Material.GREEN_WOOL,
            localizationManager.getMessage("localisation.inventory.item.accept"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.accept.open-wallet", "amount" to amount)),
            2)
        val close = systemGUI.createItem(
            Material.RED_WOOL,
            localizationManager.getMessage("localisation.inventory.item.reject"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.reject.open-wallet")),
            2)
        inventory.setItem(1, accept)
        inventory.setItem(3, close)
        return inventory
    }
}