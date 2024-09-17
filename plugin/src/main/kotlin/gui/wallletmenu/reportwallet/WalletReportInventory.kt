package gui.wallletmenu.reportwallet

import App.Companion.localizationManager
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
            Component.text(localizationManager.getMessage("localisation.inventory.title.report-menu")))
        val info = systemGUI.createItem(
            Material.TORCH,
            "Выбор проблемы",
            listOf("Выберите тип проблемы, которая у вас возникла"),
            1
        )

        val data = systemGUI.createItem(
            Material.STONE,
            "Ошибка данных",
            listOf("У вас не правильно прогружаются сообщения? и чото такое"),
            1
        )
        val work = systemGUI.createItem(
            Material.STONE,
            "Не работает",
            listOf("У вас не работает какая-либо услуга?"),
            2
        )
        val ping = systemGUI.createItem(
            Material.STONE,
            "Медленная загрузка",
            listOf("У вас слишком медленно выполняются операции?"),
            3
        )
        val other = systemGUI.createItem(
            Material.STONE,
            "Другое",
            listOf("У вас возникла другая проблема?"),
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