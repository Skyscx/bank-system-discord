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

class ConvertDiamondsBlocksAmountInventory(private val convertData: ConvertDataManager) : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {

        val title = Component.text("localisation.inventory.title.convert.amount".localized())
        val inventory = Bukkit.createInventory(null, 27, title)
        convertData.setPlayer(player)
        val convertData = convertData.getConvertData(player) ?: return systemGUI.errorInventory()
        val type = convertData.type ?: return systemGUI.errorInventory()
        val amount = convertData.amount
        val centerItem = createCenterItem(type, amount)
        // Кнопка +1
        val add1 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+1",
            customModelData = 1

        )
        // Кнопка +16
        val add16 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+16",

            customModelData = 2
        )
        // Кнопка +64
        val add64 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+64",
            customModelData = 3
        )
        // Кнопка +All
        val addAll = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+ALL",
            customModelData = 4
        )
        // Кнопка -1
        val get1 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-1",
            customModelData = 1
        )
        // Кнопка -16
        val get16 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-16",
            customModelData = 2
        )
        // Кнопка -64
        val get64 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-64",
            customModelData = 3
        )
        // Кнопка -All
        val getAll = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-ALL",
            customModelData = 4
        )

        val confirmItem = systemGUI.createItem(
            Material.GREEN_WOOL,
            "localisation.inventory.item.accept".localized(),
            customModelData = 4
        )

        // Вернуться в меню
        val backMenu = systemGUI.createItem(
            Material.DARK_OAK_DOOR,
            "localisation.inventory.item.back-wallet-menu".localized(),
            listOf("localisation.inventory.lore.wallet.back-wallet-menu".localized()),
            1
        )
        inventory.setItem(0, backMenu)

        inventory.setItem(9,getAll)
        inventory.setItem(10,get1)
        inventory.setItem(11,get16)
        inventory.setItem(12,get64)

        inventory.setItem(13, centerItem)

        inventory.setItem(14,add64)
        inventory.setItem(15,add16)
        inventory.setItem(16,add1)
        inventory.setItem(17,addAll)

        return inventory
    }

    private fun createCenterItem(type : String, amount : Int): ItemStack {
        return systemGUI.createItem(
            Material.PAPER,
            "localisation.inventory.item.info.amount",
            listOf(
                "localisation.inventory.lore.info.convert.amount".localized(
                    "type" to type, "amount" to amount.toString()
                )
            ),
            6
        )
    }

    fun updateItem(player: Player, inventory: Inventory) {
        val convertData = convertData.getConvertData(player) ?: return
        val typeBlock = convertData.type ?: return
        val amount = convertData.amount
        val centerItem = createCenterItem(typeBlock, amount)
        inventory.setItem(13, centerItem)
    }
}