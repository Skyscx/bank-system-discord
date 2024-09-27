package gui.convertdiamonds

import App.Companion.localized
import data.managers.ConvertDataManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ConvertDiamondsBlocksInventory(private val convertData: ConvertDataManager) : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {
        val title = Component.text("localisation.inventory.title.convert.type".localized())
        val inventory = Bukkit.createInventory(null, 27, title)
        convertData.setPlayer(player)
        val convertData = convertData.getConvertData(player) ?: return systemGUI.errorInventory() //todo: Протестить
        val type = convertData.type ?: return systemGUI.errorInventory()

        val centerItem = createCenterItem(type)

        val deepslateDiamond = systemGUI.createItem(
            Material.DEEPSLATE_DIAMOND_ORE,
            "localisation.deepslate_diamond_ore".localized(),
        )

        val defaultDiamond = systemGUI.createItem(
            Material.DIAMOND_ORE,
            "localisation.diamond_ore".localized(),
        )
        //
        val backMenu = systemGUI.createItem(
            Material.DARK_OAK_DOOR,
            "localisation.inventory.item.back-wallet-menu".localized(),
            customModelData = 1
        )
        inventory.setItem(0, backMenu)
        inventory.setItem(11, deepslateDiamond)
        inventory.setItem(15, defaultDiamond)
        inventory.setItem(13, centerItem)
        return inventory
    }

    private fun createCenterItem(type : String): ItemStack {
        return systemGUI.createItem(
            Material.PAPER,
            "localisation.inventory.item.info.convert".localized(),
            listOf(
                "localisation.inventory.lore.info.convert.type".localized(
                    "type" to type
                )
            ),
            6
        )
    }

    fun updateItem(player: Player, inventory: Inventory) {
        val convertData = convertData.getConvertData(player) ?: return
        val typeBlock = convertData.type ?: return
        val centerItem = createCenterItem(typeBlock)
        inventory.setItem(13, centerItem)
    }
}