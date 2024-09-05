package gui.wallletmenu.transferwallet

import App.Companion.localizationManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SelectPlayerInventory : InventoryCreator {
    private val systemGUI = SystemGUI()


    override fun createInventory(player: Player): Inventory {
        val title = Component.text(localizationManager.getMessage("localisation.inventory.title.select-player-transfer"))
        val inventory = Bukkit.createInventory(null, 54, title)

        val onlinePlayers = Bukkit.getOnlinePlayers().toList().filter { it != player }
        val pageSize = 51 // 53 slots for items, 1 slot for "next page" button
        val currentPage = 0 // You can manage the current page based on player's interaction

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(onlinePlayers.size)

        for (i in startIndex..<endIndex) {
            val playerHead = systemGUI.createPlayerHead(onlinePlayers[i], "Выбрать")
            inventory.setItem(i - startIndex + 1, playerHead) // Start from slot 1 to leave slot 0 for "Other Player" item
        }

        // Add "Other Player" item in slot 0
        val otherPlayerItem = createOtherPlayerItem()
        inventory.setItem(0, otherPlayerItem)

        // Add "Next Page" button in slot 53 if there are more players
        if (endIndex < onlinePlayers.size) {
            val nextPageItem = createNextPageItem()
            inventory.setItem(53, nextPageItem)
        }

        // Add "Previous Page" button in slot 52 if the current page is not the first page
        if (currentPage > 0) {
            val previousPageItem = createPreviousPageItem()
            inventory.setItem(52, previousPageItem)
        }

        return inventory
    }

//    private fun createPlayerHead(player: Player): ItemStack {
//        return systemGUI.createItem(
//            material = Material.PLAYER_HEAD,
//            name = player.name,
//            lore = listOf("Click to select ${player.name}"),
//            customModelData = null,
//            italic = false,
//            bold = true,
//            underlined = false,
//            strikethrough = false,
//            obfuscated = false
//        )
//    }

    private fun createOtherPlayerItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.BARRIER,
            name = localizationManager.getMessage("localisation.inventory.other-player"),
            lore = listOf("Click to select another player"),
            customModelData = null,
            italic = false,
            bold = true,
            underlined = false,
            strikethrough = false,
            obfuscated = false
        )
    }

    private fun createNextPageItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.ARROW,
            name = "Next Page",
            lore = listOf("Click to go to the next page"),
            customModelData = null,
            italic = false,
            bold = true,
            underlined = false,
            strikethrough = false,
            obfuscated = false
        )
    }

    private fun createPreviousPageItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.ARROW,
            name = "Previous Page",
            lore = listOf("Click to go to the previous page"),
            customModelData = null,
            italic = false,
            bold = true,
            underlined = false,
            strikethrough = false,
            obfuscated = false
        )
    }
}