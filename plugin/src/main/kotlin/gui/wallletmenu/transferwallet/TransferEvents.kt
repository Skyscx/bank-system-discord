package gui.wallletmenu.transferwallet

import App.Companion.instance
import App.Companion.localized
import data.managers.TransferDataManager
import functions.Functions
import gui.InventoryManager
import gui.SystemGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class TransferEvents(
    private val amountPlayerInventory: AmountPlayerInventory,
    private val confirmTransferInventory: ConfirmTransferInventory,
    private val addCommentInventory: AddCommentInventory,
) : Listener {
    private val systemGUI = SystemGUI()
    private val functions = Functions()

    // Хранилище текущей страницы для каждого игрока
    private val playerPages = mutableMapOf<Player, Int>()

    // Хранилище игроков, ожидающих ввода комментария
    private val awaitingComment = mutableSetOf<Player>()

    fun createInventory(player: Player): Inventory {
        val title = Component.text("localisation.inventory.title.select-player-transfer".localized())
        val inventory = Bukkit.createInventory(null, 54, title)

        val currentPage = playerPages.getOrDefault(player, 0)
        val pageSize = 51

        val onlinePlayers = Bukkit.getOnlinePlayers().toList().filter { it != player }

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(onlinePlayers.size)

        for (i in startIndex..<endIndex) {
            val playerHead = systemGUI.createPlayerHead(onlinePlayers[i], "localisation.select".localized())
            inventory.setItem(i - startIndex + 1, playerHead)
        }

        val otherPlayerItem = createOtherPlayerItem()
        inventory.setItem(0, otherPlayerItem)

        if (endIndex < onlinePlayers.size) {
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
                    val titleBackWalletMenu = "localisation.inventory.item.back-wallet-menu".localized()

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
                        TransferDataManager.instance.setTargetPlayer(player, textTargetPlayerName)
                        val amountInventory = amountPlayerInventory.createInventory(player)
                        player.openInventory(amountInventory)
                    } else if (functions.isComponentEqual(displayNameComponent, titleBackWalletMenu)){
                        val inventoryManager = InventoryManager()
                        inventoryManager.openInventory(player, "menu")
                    }
                }
            } else {
                 val expectedTitleAmount = "localisation.inventory.title.select-amount-transfer".localized()
                if (functions.isComponentEqual(title, expectedTitleAmount)) {
                    val currentItem = e.currentItem ?: return
                    val itemMeta = currentItem.itemMeta ?: return
                    if (itemMeta.hasDisplayName()) {
                        e.isCancelled = true
                        val displayNameComponent = itemMeta.displayName() ?: return
                        val displayNameText = PlainTextComponentSerializer.plainText().serialize(displayNameComponent)

                        if (displayNameText.startsWith("§a+")) {
                            val amount = displayNameText.substring(3).toIntOrNull() ?: return
                            val transferData = TransferDataManager.instance.getTransferData(player) ?: return
                            val newAmount = transferData.amount + amount
                            TransferDataManager.instance.setAmount(player, newAmount)
                            amountPlayerInventory.updateItem(player, player.openInventory.topInventory)
                        } else if (displayNameText.startsWith("§4-")) {
                            val amount = displayNameText.substring(3).toIntOrNull() ?: return
                            val transferData = TransferDataManager.instance.getTransferData(player) ?: return
                            val newAmount = transferData.amount - amount
                            if (newAmount < 0) {
                                TransferDataManager.instance.setAmount(player, 0)
                            } else {
                                TransferDataManager.instance.setAmount(player, newAmount)
                            }
                            amountPlayerInventory.updateItem(player, player.openInventory.topInventory)
                        } else if (displayNameText == "localisation.inventory.item.confirm-amount".localized()) {
                            val addCommentInventory = addCommentInventory.createInventory(player)
                            player.openInventory(addCommentInventory)
                        }
                    }
                } else {
                    val expectedTitleAddComment = "localisation.inventory.title.add-comment".localized()
                    if (functions.isComponentEqual(title, expectedTitleAddComment)) {
                        val currentItem = e.currentItem ?: return
                        val itemMeta = currentItem.itemMeta ?: return
                        if (itemMeta.hasDisplayName()) {
                            e.isCancelled = true
                            val displayNameComponent = itemMeta.displayName() ?: return
                            if (functions.isComponentEqual(displayNameComponent, "localisation.inventory.item.add-comment".localized())) {
                                player.closeInventory()
                                awaitingComment.add(player)
                                openAnvilGUI(player)

                            } else if (functions.isComponentEqual(displayNameComponent, "localisation.inventory.item.confirm-without-comment".localized())) {
                                val confirmInventory = confirmTransferInventory.createInventory(player)
                                player.openInventory(confirmInventory)
                            }
                        }
                    } else {
                        val expectedTitleConfirm = "localisation.inventory.title.confirm-transfer".localized()
                        if (functions.isComponentEqual(title, expectedTitleConfirm)) {
                            val currentItem = e.currentItem ?: return
                            val itemMeta = currentItem.itemMeta ?: return
                            if (itemMeta.hasDisplayName()) {
                                e.isCancelled = true
                                val displayNameComponent = itemMeta.displayName() ?: return
                                if (functions.isComponentEqual(displayNameComponent, "localisation.inventory.item.accept".localized())) {
                                    val transferData = TransferDataManager.instance.getTransferData(player) ?: return
                                    Bukkit.dispatchCommand(player, "transfer ${transferData.targetPlayerName} ${transferData.amount} ${transferData.comment ?: ""}")
                                    TransferDataManager.instance.removeTransferData(player)
                                    player.closeInventory()
                                } else if (functions.isComponentEqual(displayNameComponent, "localisation.inventory.item.reject".localized())) {
                                    TransferDataManager.instance.removeTransferData(player)
                                    player.closeInventory()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openAnvilGUI(player: Player) {
        val item = systemGUI.createItem(
            Material.PAPER,
            name = "localisation.comment".localized(),
            customModelData = 2

        )
        AnvilGUI.Builder()
            .onClick { slot, stateSnapshot ->
                if (slot == AnvilGUI.Slot.OUTPUT) {
                    val text = stateSnapshot.text
                    TransferDataManager.instance.setComment(player, text)
                    awaitingComment.remove(player)

                    val confirmInventory = confirmTransferInventory.createInventory(player)
                    player.openInventory(confirmInventory)

                    return@onClick listOf(AnvilGUI.ResponseAction.close())
                }
                return@onClick emptyList()
            }
            .text(" ")
            .itemLeft(item)
            .title("localisation.inventory.title.add-comment".localized())
            .plugin(instance)
            .open(player)

    }


    private fun createOtherPlayerItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.BARRIER,
            name = "localisation.other-target".localized(),
            lore = listOf("localisation.inventory.lore.other-player.transfer-menu".localized()),
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

}