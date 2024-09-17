package gui.wallletmenu.actionwallet

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import data.ActionDataManager
import functions.Functions
import gui.InventoryManager
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import kotlin.math.absoluteValue

class WalletActionsInventoryEvent(
    private val walletActionsInventory: WalletActionsInventory
):Listener {
    private val functions = Functions()
    private val inventoryManager = InventoryManager()
//    private val walletActionsInventory = WalletActionsInventory()

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
            "§4-ALL" to 0,
            localizationManager.getMessage("localisation.inventory.item.back-wallet-menu") to "menu",
            "Выполнить" to "confirm"
        )

        val action = titleMap[displayName]

        if (action == "menu") {
            inventoryManager.openInventory(player, "menu")
            ActionDataManager.instance.removeActionData(player)
            return
        }
        if (action == "confirm"){
            val actionData = ActionDataManager.instance.getActionData(player) ?: return
            val amount = actionData.amount
            if (amount > 0) {
            player.performCommand("wallet balance add $amount")
        } else if (amount < 0) {
            val absoluteAmount = amount.absoluteValue
            player.performCommand("wallet balance remove $absoluteAmount")
        } else {
            player.sendMessage("[DEV] Функция в разработке")
        }
            player.closeInventory()
            ActionDataManager.instance.removeActionData(player)
            return
        }

        val amount = action as? Int ?: return


        val uuid = userDB.getUUIDbyPlayerName(player.name) ?: return
        val walletDefault = userDB.getDefaultWalletByUUID(uuid) ?: return
        val walletCurrency = walletDB.getWalletCurrency(walletDefault) ?: return
        val currency = functions.convertStringToMaterial(walletCurrency)
        val typeBlock = currency.first ?: return //todo: Потом реализовать

        if (!currency.second) {
            player.sendMessage("Ошибка инициализации валюты")
            return
        }
        val actionData = ActionDataManager.instance.getActionData(player) ?: return
        val newAmount = actionData.amount + amount
        ActionDataManager.instance.setAmount(player, newAmount)
        walletActionsInventory.updateItem(player, player.openInventory.topInventory)

    }

}