package gui.wallletmenu.actionwallet

import App
import App.Companion.historyDB
import App.Companion.localized
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
        val title = Component.text("localisation.inventory.title.history".localized())
        val inventory = Bukkit.createInventory(null, 54, title)

        val currentPage = playerPages.getOrDefault(player, 0)
        val pageSize = 51

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
                        val description = when (typeOperation) {
                            "TRANSFER" -> {
                                val isSender = senderUUID == userUUID
                                if (isSender) {
                                    listOf(
                                        "localisation.inventory.lore.history.transfer.sending".localized(
                                            "amount" to amount.toString(), "currency" to currency),
                                        "localisation.inventory.lore.history.player_v1".localized("name" to targetName),
                                        "localisation.inventory.lore.history.transfer.comment".localized("comment" to comment)
                                    )
                                } else {
                                    listOf(
                                        "localisation.inventory.lore.history.transfer.taked".localized(
                                            "amount" to amount.toString(), "currency" to currency),
                                        "localisation.inventory.lore.history.player_v2".localized("name" to targetName),
                                        "localisation.inventory.lore.history.transfer.comment".localized("comment" to comment)
                                    )
                                }
                            }
                            "OPEN_WALLET" -> listOf(
                                "localisation.inventory.lore.history.open_wallet".localized(),
                                "localisation.inventory.lore.history.player_v3".localized("name" to senderName)
                            )
                            "ATTEMPT_OPEN_WALLET" -> listOf(
                                "localisation.inventory.lore.history.open_wallet_attempt".localized(),
                                "localisation.inventory.lore.history.player_v3".localized("name" to senderName)
                            )
                            "RENAMING" -> listOf(
                                "localisation.inventory.lore.history.renaming".localized(),
                                "localisation.inventory.lore.history.player_v3".localized("name" to senderName)
                            )
                            "CLOSE_WALLET" -> listOf(
                                "localisation.inventory.lore.history.close_wallet".localized(),
                                "localisation.inventory.lore.history.player_v3".localized("name" to senderName)
                            )
                            "ADD_BALANCE" -> listOf(
                                "localisation.inventory.lore.history.balance.add".localized(),
                                "localisation.inventory.lore.history.balance.value.add".localized(
                                    "balance" to oldBalance.toString(), "newBalance" to (newBalance - oldBalance).toString()),)
                            "GET_BALANCE" -> listOf(
                                "localisation.inventory.lore.history.balance.get".localized(),
                                "localisation.inventory.lore.history.balance.value.add".localized(
                                    "balance" to oldBalance.toString(), "newBalance" to (oldBalance - newBalance).toString())
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
                            "localisation.inventory.item.back-wallet-menu".localized(),
                            listOf("localisation.inventory.lore.wallet.back-wallet-menu".localized()),
                            1
                        )
                        inventory.setItem(0, backMenu)
                        inventory.setItem(index + 1, icon)
                    }
                    if (result.size == pageSize) {
                        val nextPageItem = createNextPageItem()
                        inventory.setItem(53, nextPageItem)
                    }
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
            val expectedTitle = "localisation.inventory.title.history".localized()
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled = true
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleNextPage = "localisation.next-page".localized()
                    val titlePreviousPage = "localisation.previous-page".localized()
                    val titleBackMenu = "localisation.inventory.item.back-wallet-menu".localized()
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
