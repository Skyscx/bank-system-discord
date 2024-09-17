package gui.wallletmenu.actionwallet

import App
import App.Companion.historyDB
import App.Companion.localizationManager
import App.Companion.walletDB
import functions.Functions
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.sql.SQLException
import java.text.SimpleDateFormat

class WalletHistoryInventory : InventoryCreator, Listener {
    private val systemGUI = SystemGUI()
    private val functions = Functions()

    // Хранилище текущей страницы для каждого игрока
    private val playerPages = mutableMapOf<Player, Int>()

    override fun createInventory(player: Player): Inventory {
        val title = Component.text("История транзакций")
        val inventory = Bukkit.createInventory(null, 54, title)

        val currentPage = playerPages.getOrDefault(player, 0)
        val pageSize = 51 // 53 slots for items, 1 slot for "next page" button

        val userUUID = player.uniqueId.toString()
        val offset = currentPage * pageSize

        Bukkit.getScheduler().runTaskAsynchronously(App.instance, Runnable {
            try {
                val result = historyDB.getUserHistory(userUUID, pageSize, offset)

                Bukkit.getScheduler().runTask(App.instance, Runnable {
                    for ((index, row) in result.withIndex()) {
                        val senderUUID = row["SenderUUID"] as String
                        val targetUUID = row["TargetUUID"] as String
                        val amount = row["Amount"] as Int
                        val currency = row["Currency"] as String
                        val senderName = row["SenderName"] as String
                        val targetName = row["TargetName"] as String
                        val dateStr = row["Date"] as String
                        val comment = row["Comment"] as String
                        val typeOperation = row["TypeOperation"] as String
                        val walletIdSender = row["SenderIdWallet"] as Int
                        val oldBalance = row["OldBalance"] as Int
                        val newBalance = row["NewBalance"] as Int

                        val date = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").parse(dateStr)
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                        val formattedDate = dateFormat.format(date)
                        val balance = walletDB.getWalletBalance(walletIdSender.toInt()) ?: 0

                        //val iconUUID = if (senderUUID == userUUID) targetUUID else senderUUID

                        val description = when (typeOperation) {
                            "TRANSFER" -> listOf(
                                "Отправлено - $amount $currency",
                                "Игроку - $targetName",
                                "Комментарий: $comment"
                            )
                            "OPEN_WALLET" -> listOf(
                                "Открыт кошелек",
                                "Игроком - $senderName"
                            )
                            "ATTEMPT_OPEN_WALLET" -> listOf(
                                "Попытка открыть кошелек",
                                "Игроком - $senderName"
                            )
                            "RENAMING" -> listOf(
                                "Переименование кошелька",
                                "Игроком - $senderName"
                            )
                            "CLOSE_WALLET" -> listOf(
                                "Закрыт кошелек",
                                "Игроком - $senderName"
                            )
                            "ADD_BALANCE" -> listOf(
                                "Пополнение баланса",
                                "Баланс - $oldBalance (+${newBalance-oldBalance})"
                            )
                            "GET_BALANCE" -> listOf(
                                "Снятие баланса",
                                "Баланс - $oldBalance (-${oldBalance-newBalance})"
                            )
                            else -> listOf()
                        }

                        val material = when (typeOperation) {
                            "TRANSFER" -> Material.PAPER
                            "OPEN_WALLET" -> Material.GREEN_STAINED_GLASS_PANE
                            "ATTEMPT_OPEN_WALLET" -> Material.YELLOW_STAINED_GLASS_PANE
                            "RENAMING" -> Material.NAME_TAG
                            "CLOSE_WALLET" -> Material.RED_STAINED_GLASS_PANE
                            "ADD_BALANCE" -> Material.GREEN_WOOL
                            "GET_BALANCE" -> Material.ORANGE_WOOL
                            else -> Material.BARRIER
                        }

                        val icon = systemGUI.createItem(
                            material,
                            formattedDate,
                            description,
                            1
                        )

                        val backMenu = systemGUI.createItem(
                            Material.DARK_OAK_DOOR,
                            localizationManager.getMessage("localisation.inventory.item.back-wallet-menu"),
                            listOf(localizationManager.getMessage("localisation.inventory.lore.wallet.back-wallet-menu")),
                            1
                        )
                        inventory.setItem(0, backMenu)
                        inventory.setItem(index + 1, icon) // Start from slot 1 to leave slot 0 for "Other Player" item
                    }

                    // Add "Next Page" button in slot 53 if there are more items
                    if (result.size == pageSize) {
                        val nextPageItem = createNextPageItem()
                        inventory.setItem(53, nextPageItem)
                    }

                    // Add "Previous Page" button in slot 52 if the current page is not the first page
                    if (currentPage > 0) {
                        val previousPageItem = createPreviousPageItem()
                        inventory.setItem(52, previousPageItem)
                    }

                    player.openInventory(inventory)
                })
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        })

        return inventory
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.CHEST) {
            val title = e.view.title()
            val expectedTitle = "История транзакций"
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleNextPage = localizationManager.getMessage("localisation.next-page")
                    val titlePreviousPage = localizationManager.getMessage("localisation.previous-page")
                    val titleBackMenu = localizationManager.getMessage("localisation.inventory.item.back-wallet-menu")
                    if (functions.isComponentEqual(displayNameComponent, titleNextPage)) {
                        playerPages[player] = playerPages.getOrDefault(player, 0) + 1
                        player.openInventory(createInventory(player))
                    } else if (functions.isComponentEqual(displayNameComponent, titlePreviousPage)) {
                        playerPages[player] = playerPages.getOrDefault(player, 0) - 1
                        player.openInventory(createInventory(player))
                    } else if (functions.isComponentEqual(displayNameComponent, titleBackMenu)){
                        player.performCommand("wallet")
                    }
                }
            }
        }
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
