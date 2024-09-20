package gui.wallletmenu.transferwallet

import App.Companion.localized
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
        val inventory = Bukkit.createInventory(null, 54, Component.text("localisation.inventory.title.add-comment".localized()))


        val addCommentItem = systemGUI.createItem(
            Material.PAPER,
            "localisation.inventory.item.add-comment".localized(),
            listOf("localisation.inventory.lore.item.add-comment.transfer-menu".localized()),
            2
        )
        val confirmWithoutCommentItem = systemGUI.createItem(
            Material.GREEN_WOOL,
            "localisation.inventory.item.confirm-without-comment".localized(),
            listOf("localisation.inventory.lore.item.confirm-without-comment.transfer-menu".localized()),
            3
        )

        inventory.setItem(20, addCommentItem)
        inventory.setItem(24, confirmWithoutCommentItem)

        return inventory
    }
}