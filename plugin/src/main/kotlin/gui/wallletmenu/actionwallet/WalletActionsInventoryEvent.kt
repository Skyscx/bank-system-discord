package gui.wallletmenu.actionwallet

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import functions.Functions
import gui.InventoryManager
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class WalletActionsInventoryEvent:Listener {
    private val functions = Functions()
    private val inventoryManager = InventoryManager()
    //private val discordNotifier = DiscordNotifier(discordBot.getJDA(), config)
    //private val countAccountConfig = config.getInt("count-free-accounts") TODO: Вернуть в будущем когда будет система разных кошельков.

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.CHEST) {
            val title = e.view.title()
            val expectedTitle = localizationManager.getMessage("localisation.inventory.title.menu-actions-wallet")
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    val displayName = itemMeta.displayName()
                    val plainTextSerializer = PlainTextComponentSerializer.plainText()
                    val displayNameText = plainTextSerializer.serialize(displayName!!)
                    handleClick(player, displayNameText)
                }
                e.isCancelled = true
            }
        }
    }

    private fun handleClick(player: Player, displayName: String) {
        val titleMap = mapOf(
            "§a+1" to 1,
            "§a+16" to 16,
            "§a+64" to 64,
            "§a+ALL" to 0,
            "§4-1" to -1,
            "§4-16" to -16,
            "§4-64" to -64,
            "§4-ALL" to 0
        )

        val amount = titleMap[displayName] ?: return

        val uuid = userDB.getUUIDbyPlayerName(player.name) ?: return
        val walletDefault = userDB.getDefaultWalletByUUID(uuid) ?: return
        val walletCurrency = walletDB.getWalletCurrency(walletDefault) ?: return
        val currency = functions.convertStringToMaterial(walletCurrency)
        val typeBlock = currency.first ?: return

        if (!currency.second) {
            player.sendMessage("Ошибка иницилизации валюты")
            return
        }

        if (amount > 0) {
            handleAddBalance(player, walletDefault, typeBlock, amount)
        } else if (amount < 0) {
            handleGetBalance(player, walletDefault, typeBlock, amount)
        } else {
            player.sendMessage("[DEV] Функция в разработке")
        }
    }

    private fun handleAddBalance(player: Player, walletDefault: Int, typeBlock: Material, amount: Int) {
        val countPlayerBlock = functions.countBlocksInInventory(player, typeBlock)
        if (amount > countPlayerBlock) {
            player.sendMessage("У вас нет столько предметов в инвентаре.")
            return
        }
        functions.takeItem(player, typeBlock, amount)
        walletDB.updateWalletBalance(walletDefault, amount)
    }

    private fun handleGetBalance(player: Player, walletDefault: Int, typeBlock: Material, amount: Int) {
        val balance = walletDB.getWalletBalance(walletDefault) ?: 0
        if (balance < -amount) {
            player.sendMessage("У вас недостаточно средств на балансе.")
            return
        }

        val item = ItemStack(typeBlock)
        val successful = functions.giveItem(player, item, -amount)
        if (successful) {
            walletDB.updateWalletBalance(walletDefault, amount)
            player.sendMessage("Вы сняли ${-amount} $typeBlock")
        } else {
            player.sendMessage("Операция прервана")
        }
    }
}