package gui.convertdiamonds

import App.Companion.localized
import data.managers.ConvertDataManager
import functions.Functions
import gui.InventoryManager
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class ConvertEvents(
    private val convertDiamondsBlocksAmountInventory: ConvertDiamondsBlocksAmountInventory,
    private val convertDiamondsBlocksInventory: ConvertDiamondsBlocksInventory
) : Listener{
    private val functions = Functions()
    private val inventoryManager = InventoryManager()
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.CHEST) {
            val title = e.view.title()
            val expectedTitleType = "localisation.inventory.title.convert.type".localized()
            val expectedTitleAmount = "localisation.inventory.title.convert.amount".localized()
            if (functions.isComponentEqual(title, expectedTitleType)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    val displayName = itemMeta.displayName()
                    val plainTextSerializer = PlainTextComponentSerializer.plainText()
                    val displayNameText = plainTextSerializer.serialize(displayName!!)
                    handleClickType(player, displayNameText)
                }
                e.isCancelled = true
            }
            if (functions.isComponentEqual(title, expectedTitleAmount)){
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    val displayName = itemMeta.displayName()
                    val plainTextSerializer = PlainTextComponentSerializer.plainText()
                    val displayNameText = plainTextSerializer.serialize(displayName!!)
                    handleClickAmount(player, displayNameText, currentItem.type)
                }
                e.isCancelled = true
            }
        }
    }

    private fun handleClickType(player: Player, displayName: String) {
        val titleMap = mapOf(
            "localisation.diamond_ore".localized() to "DIAMOND_ORE",
            "localisation.deepslate_diamond_ore".localized() to "DEEPSLATE_DIAMOND_ORE",
            "localisation.inventory.item.back-wallet-menu".localized() to "menu",
            "localisation.inventory.item.info.convert".localized() to "amount"
        )
        val action = titleMap[displayName]

        if (action == "menu"){
            ConvertDataManager.instance.removeConvertData(player)
            inventoryManager.openInventory(player, "menu")
            return
        }
        if (action == "amount"){
            val amountInventory = convertDiamondsBlocksAmountInventory.createInventory(player)
            player.openInventory(amountInventory)
            return
        }

        if (action != null) {
            ConvertDataManager.instance.setType(player, action)
            convertDiamondsBlocksInventory.updateItem(player, player.openInventory.topInventory)
        }
        return
    }

    private fun handleClickAmount(player: Player, displayName: String, typeClick: Material) {
        val titleMap = mapOf(
            "§a+1" to 1,
            "§a+16" to 16,
            "§a+64" to 64,
            "§a+ALL" to functions.countBlocksInInventory(player, typeClick),
            "§4-1" to -1,
            "§4-16" to -16,
            "§4-64" to -64,
            "§4-ALL" to 0,
            "localisation.inventory.item.info.amount".localized() to "accept",
            "localisation.inventory.item.back-wallet-menu".localized() to "menu"
        )
        val action = titleMap[displayName]
        if (action == "menu"){
            ConvertDataManager.instance.removeConvertData(player)
            inventoryManager.openInventory(player, "menu")
            return
        }
        if (action == "accept"){
            player.closeInventory()
            val convertData = ConvertDataManager.instance.getConvertData(player) ?: return
            val amountData = convertData.amount
            if (amountData <= 0) {
                player.sendMessage("localisation.messages.out.converted-null".localized())
                return
            }
            val typeData = convertData.type ?: return
            player.performCommand("convert-diamonds $typeData $amountData")
            ConvertDataManager.instance.removeConvertData(player)
            inventoryManager.openInventory(player, "menu")
            return
        }

        val amount = action as? Int ?: return
        val convertData = ConvertDataManager.instance.getConvertData(player) ?: return
        val amountData = convertData.amount
        val typeData = convertData.type ?: return
        val newAmount = amountData + amount
        if (newAmount > functions.countBlocksInInventory(player, Material.valueOf(typeData.uppercase())) || newAmount < 0) return
        ConvertDataManager.instance.setAmount(player, newAmount)
        convertDiamondsBlocksAmountInventory.updateItem(player, player.openInventory.topInventory)
        return
    }

}