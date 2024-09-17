package gui.wallletmenu.transferwallet

import App.Companion.instance
import App.Companion.localizationManager
import data.TransferDataManager
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
//    private val iTextCommand: ITextCommand
) : Listener {
    private val systemGUI = SystemGUI()
    private val functions = Functions()

    // Хранилище текущей страницы для каждого игрока
    private val playerPages = mutableMapOf<Player, Int>()

    // Хранилище игроков, ожидающих ввода комментария
    private val awaitingComment = mutableSetOf<Player>()

    fun createInventory(player: Player): Inventory {
        val title = Component.text(localizationManager.getMessage("localisation.inventory.title.select-player-transfer"))
        val inventory = Bukkit.createInventory(null, 54, title)

        val currentPage = playerPages.getOrDefault(player, 0)
        val pageSize = 51 // 53 slots for items, 1 slot for "next page" button

        val onlinePlayers = Bukkit.getOnlinePlayers().toList().filter { it != player }

        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(onlinePlayers.size)

        for (i in startIndex..<endIndex) {
            val playerHead = systemGUI.createPlayerHead(onlinePlayers[i], localizationManager.getMessage("localisation.select"))
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
                    val titleBackWalletMenu = localizationManager.getMessage("localisation.inventory.item.back-wallet-menu")

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
                 val expectedTitleAmount = localizationManager.getMessage("localisation.inventory.title.select-amount-transfer")
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
                        } else if (displayNameText == localizationManager.getMessage("localisation.inventory.item.confirm-amount")) {
                            val addCommentInventory = addCommentInventory.createInventory(player)
                            player.openInventory(addCommentInventory)
                        }
                    }
                } else {
                    val expectedTitleAddComment = localizationManager.getMessage("localisation.inventory.title.add-comment")
                    if (functions.isComponentEqual(title, expectedTitleAddComment)) {
                        val currentItem = e.currentItem ?: return
                        val itemMeta = currentItem.itemMeta ?: return
                        if (itemMeta.hasDisplayName()) {
                            e.isCancelled = true
                            val displayNameComponent = itemMeta.displayName() ?: return
                            if (functions.isComponentEqual(displayNameComponent, localizationManager.getMessage("localisation.inventory.item.add-comment"))) {
                                player.closeInventory()
//                                player.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer.input-comment"))
                                awaitingComment.add(player)
                                openAnvilGUI(player)

                            } else if (functions.isComponentEqual(displayNameComponent, localizationManager.getMessage("localisation.inventory.item.confirm-without-comment"))) {
                                val confirmInventory = confirmTransferInventory.createInventory(player)
                                player.openInventory(confirmInventory)
                            }
                        }
                    } else {
                        val expectedTitleConfirm = localizationManager.getMessage("localisation.inventory.title.confirm-transfer")
                        if (functions.isComponentEqual(title, expectedTitleConfirm)) {
                            val currentItem = e.currentItem ?: return
                            val itemMeta = currentItem.itemMeta ?: return
                            if (itemMeta.hasDisplayName()) {
                                e.isCancelled = true
                                val displayNameComponent = itemMeta.displayName() ?: return
                                if (functions.isComponentEqual(displayNameComponent, localizationManager.getMessage("localisation.inventory.item.accept"))) {
                                    val transferData = TransferDataManager.instance.getTransferData(player) ?: return
                                    Bukkit.dispatchCommand(player, "transfer ${transferData.targetPlayerName} ${transferData.amount} ${transferData.comment ?: ""}")
                                    TransferDataManager.instance.removeTransferData(player)
                                    player.closeInventory()
                                } else if (functions.isComponentEqual(displayNameComponent, localizationManager.getMessage("localisation.inventory.item.reject"))) {
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
            name = localizationManager.getMessage("localisation.comment"),
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
            .title(localizationManager.getMessage("localisation.inventory.title.add-comment"))
            .plugin(instance)
            .open(player)

    }


    private fun createOtherPlayerItem(): ItemStack {
        return systemGUI.createItem(
            material = Material.BARRIER,
            name = localizationManager.getMessage("localisation.other-target"),
            lore = listOf(localizationManager.getMessage("localisation.inventory.lore.other-player.transfer-menu")),
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

}