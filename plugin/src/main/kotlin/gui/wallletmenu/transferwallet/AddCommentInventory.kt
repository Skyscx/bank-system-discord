package gui.wallletmenu.transferwallet

import App.Companion.localizationManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class AddCommentInventory : InventoryCreator {
    private val systemGUI = SystemGUI()

    override fun createInventory(player: Player): Inventory {
        val inventory = Bukkit.createInventory(null, 54, Component.text(localizationManager.getMessage("localisation.inventory.title.add-comment")))


        val addCommentItem = systemGUI.createItem(
            Material.PAPER,
            localizationManager.getMessage("localisation.inventory.item.add-comment"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.add-comment.transfer-menu")),
            2
        )
        val confirmWithoutCommentItem = systemGUI.createItem(
            Material.GREEN_WOOL,
            localizationManager.getMessage("localisation.inventory.item.confirm-without-comment"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.item.confirm-without-comment.transfer-menu")),
            3
        )

        inventory.setItem(20, addCommentItem)
        inventory.setItem(24, confirmWithoutCommentItem)

        return inventory
    }
}