package gui.wallletmenu.transferwallet

import App.Companion.localizationManager
import App.Companion.userDB
import data.TransferDataManager
import functions.Functions
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class SelectPlayerInventory(private val transferDataManager: TransferDataManager) : InventoryCreator, Listener {
    private val systemGUI = SystemGUI()
    private val functions = Functions()

    // Хранилище текущей страницы для каждого игрока
    private val playerPages = mutableMapOf<Player, Int>()

    override fun createInventory(player: Player): Inventory {
        val title = Component.text(localizationManager.getMessage("localisation.inventory.title.select-player-transfer"))
        val inventory = Bukkit.createInventory(null, 54, title)

        val currentPage = playerPages.getOrDefault(player, 0)
        val pageSize = 51 // 53 slots for items, 1 slot for "next page" button

        val allPlayers = userDB.getAllPlayers()
        val onlinePlayers = Bukkit.getOnlinePlayers().map { it.uniqueId.toString() }.toSet()

        // Сортировка игроков: онлайн-игроки в начале списка
        val sortedPlayers = allPlayers.sortedByDescending { onlinePlayers.contains(it["UUID"]) }

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(sortedPlayers.size)

        for (i in startIndex..<endIndex) {
            val playerData = sortedPlayers[i]
            val playerUUID = playerData["UUID"] as String
            val playerName = playerData["PlayerName"] as String
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID))
            val playerHead = systemGUI.createPlayerHead(offlinePlayer, localizationManager.getMessage("localisation.select"))
            inventory.setItem(i - startIndex + 1, playerHead) // Start from slot 1 to leave slot 0 for "Other Player" item
        }

        // Add "Other Player" item in slot 0
        val backWalletMenu = createBackWalletMenu()
        inventory.setItem(0, backWalletMenu)

        // Add "Next Page" button in slot 53 if there are more players
        if (endIndex < sortedPlayers.size) {
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

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.CHEST) {
            val title = e.view.title()
            val expectedTitle = localizationManager.getMessage("localisation.inventory.title.select-player-transfer")
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleNextPage = localizationManager.getMessage("localisation.next-page")
                    val titlePreviousPage = localizationManager.getMessage("localisation.previous-page")

                    if (functions.isComponentEqual(displayNameComponent, titleNextPage)) {
                        playerPages[player] = playerPages.getOrDefault(player, 0) + 1
                        player.openInventory(createInventory(player))
                    } else if (functions.isComponentEqual(displayNameComponent, titlePreviousPage)) {
                        playerPages[player] = playerPages.getOrDefault(player, 0) - 1
                        player.openInventory(createInventory(player))
                    } else if (currentItem.type == Material.PLAYER_HEAD) {
                        // Кликнули по голове игрока
                        val targetPlayerName = itemMeta.displayName() ?: return
                        val textTargetPlayerName = PlainTextComponentSerializer.plainText().serialize(targetPlayerName)
                        transferDataManager.setTargetPlayer(player, textTargetPlayerName)
                        val amountInventory = AmountPlayerInventory(transferDataManager).createInventory(player)
                        player.openInventory(amountInventory)
                    }
                }
            }
        }
    }

    private fun createBackWalletMenu(): ItemStack {
        return systemGUI.createItem(
            Material.DARK_OAK_DOOR,
            localizationManager.getMessage("localisation.inventory.item.back-wallet-menu"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wallet.back-wallet-menu")),
1
            )
    }

    private fun createNextPageItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.ARROW,
            name = localizationManager.getMessage("localisation.next-page"),
            lore = listOf(localizationManager.getMessage("localisation.inventory.lore.next-page")),
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
            name = localizationManager.getMessage("localisation.previous-page"),
            lore = listOf(localizationManager.getMessage("localisation.inventory.lore.previous-page")),
            customModelData = null,
            italic = false,
            bold = true,
            underlined = false,
            strikethrough = false,
            obfuscated = false
        )
    }

    fun openInitialInventory(player: Player) {
        player.openInventory(createInventory(player))
    }
}
