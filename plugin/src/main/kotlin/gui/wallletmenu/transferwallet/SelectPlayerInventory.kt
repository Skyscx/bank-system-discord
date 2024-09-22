package gui.wallletmenu.transferwallet

import App.Companion.localized
import App.Companion.userDB
import data.managers.TransferDataManager
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
        val title = Component.text("localisation.inventory.title.select-player-transfer".localized())
        val inventory = Bukkit.createInventory(null, 54, title)

        val currentPage = playerPages.getOrDefault(player, 0)
        val pageSize = 51

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

            // Пропускаем голову игрока, который открыл инвентарь
            if (playerUUID == player.uniqueId.toString()) continue

            val playerHead = systemGUI.createPlayerHead(offlinePlayer, "localisation.select".localized())
            inventory.setItem(i - startIndex + 1, playerHead)
        }

        val backWalletMenu = createBackWalletMenu()
        inventory.setItem(0, backWalletMenu)

        if (endIndex < sortedPlayers.size) {
            val nextPageItem = createNextPageItem()
            inventory.setItem(53, nextPageItem)
        }

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
            val expectedTitle = "localisation.inventory.title.select-player-transfer".localized()
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleNextPage = "localisation.next-page".localized()
                    val titlePreviousPage = "localisation.previous-page".localized()

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
            "localisation.inventory.item.back-wallet-menu".localized(),
            listOf("localisation.inventory.lore.wallet.back-wallet-menu".localized()),
            1
        )
    }

    private fun createNextPageItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.ARROW,
            name = "localisation.next-page".localized(),
            lore = listOf("localisation.inventory.lore.next-page".localized()),
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
            name = "localisation.previous-page".localized(),
            lore = listOf("localisation.inventory.lore.previous-page".localized()),
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
