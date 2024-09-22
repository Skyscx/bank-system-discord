package gui.wallletmenu.reportwallet

import App.Companion.localized
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class WalletReportInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {

        val inventory = Bukkit.createInventory(null, 27,
            Component.text("localisation.inventory.title.report-menu".localized()))
        val info = systemGUI.createItem(
            Material.TORCH,
            "localisation.inventory.item.choice-report".localized(),
            listOf("localisation.inventory.lore.choice-report.report".localized()),
            1
        )

        val data = systemGUI.createItem(
            Material.STONE,
            "localisation.report.type.data".localized(),
            listOf("localisation.inventory.lore.data.report".localized()),
            1
        )
        val work = systemGUI.createItem(
            Material.STONE,
            "localisation.report.type.work".localized(),
            listOf("localisation.inventory.lore.work.report".localized()),
            2
        )
        val ping = systemGUI.createItem(
            Material.STONE,
            "localisation.report.type.ping".localized(),
            listOf("localisation.inventory.lore.ping.report".localized()),
            3
        )
        val other = systemGUI.createItem(
            Material.STONE,
            "localisation.report.type.other".localized(),
            listOf("localisation.inventory.lore.other.report".localized()),
            4
        )

        inventory.setItem(10, info)
        inventory.setItem(12, data)
        inventory.setItem(13, work)
        inventory.setItem(14, ping)
        inventory.setItem(15, other)
        return inventory
    }
}