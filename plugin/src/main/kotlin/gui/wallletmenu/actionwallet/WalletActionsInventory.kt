package gui.wallletmenu.actionwallet

import App.Companion.localizationManager
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import data.ActionDataManager
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WalletActionsInventory(private val actionData: ActionDataManager) : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {
        //todo: сделать получение цены создания кошелька.
        val title = Component.text("localisation.inventory.title.menu-actions-wallet".localized())
        val inventory = Bukkit.createInventory(null, 27, title)
        val uuid = player.uniqueId.toString()
        val walletD = userDB.getDefaultWalletByUUID(uuid) ?: 0
        val currency = walletDB.getWalletCurrency(walletD) ?: "[Missing currency]"
        val balance = walletDB.getWalletBalance(walletD) ?: "[Missing balance]"
        val dateReg = walletDB.getVerificationWalletDate(walletD) ?: "[Missing dateReg]"
        actionData.setPlayer(player, 0)
        val actionData = actionData.getActionData(player) ?: return Bukkit.createInventory(null, 54, Component.text(
            "localisation.error".localized()))
            // Кнопка пополнения 1
        val addBalance1 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+1",
            listOf("localisation.inventory.lore.add-balance.actions-menu".localized(
                "amount" to "1", "currencyName" to currency)),
            1
        )
        // Кнопка пополнения 16
        val addBalance16 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+16",
            listOf(localizationManager.getMessage("localisation.inventory.lore.add-balance.actions-menu",
                "amount" to "16", "currencyName" to currency)),
            2
        )
        // Кнопка пополнения 64
        val addBalance64 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+64",
            listOf("localisation.inventory.lore.add-balance.actions-menu".localized(
                "amount" to "64", "currencyName" to currency)),
            3
        )
        // Кнопка пополнения All
        val addBalanceAll = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+ALL",
            listOf("localisation.inventory.lore.add-balance.actions-menu".localized(
                "amount" to "[DEV]", "currencyName" to "[DEV]")),
            4
        )
        // Кнопка снятия 1
        val getBalance1 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-1",
            listOf("localisation.inventory.lore.get-balance.actions-menu".localized(
                "amount" to "1", "currencyName" to currency)),
            1
        )
        // Кнопка снятия 16
        val getBalance16 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-16",
            listOf("localisation.inventory.lore.get-balance.actions-menu".localized(
                "amount" to "16", "currencyName" to currency)),
            2
        )
        // Кнопка снятия 64
        val getBalance64 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-64",
            listOf("localisation.inventory.lore.get-balance.actions-menu".localized(
                "amount" to "64", "currencyName" to currency)),
            3
        )
        // Кнопка снятия All
        val getBalanceAll = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-ALL",
            listOf("localisation.inventory.lore.get-balance.actions-menu".localized(
                "amount" to "[DEV]", "currencyName" to "[DEV]")),
            4
        )
        // Кнопка кошелька
        val amount = actionData.amount
        val wallet = createWalletItem("ОЖИДАНИЕ", actionData.amount) //todo: ЛОКАЛИЗАЦИЮ

        // Кнопка "Произвести транзакцию" todo: СДЕЛАТЬ
        val confirmItem = systemGUI.createItem(
            Material.GREEN_WOOL,
            "Выполнить",
            customModelData = 4
        )
        // Вернуться в меню
        val backMenu = systemGUI.createItem(
            Material.DARK_OAK_DOOR,
            "localisation.inventory.item.back-wallet-menu".localized(),
            listOf("localisation.inventory.lore.wallet.back-wallet-menu".localized()),
            1
        )

        inventory.setItem(4, backMenu)

        inventory.setItem(12, addBalance1)
        inventory.setItem(11, addBalance16)
        inventory.setItem(10, addBalance64)
        //inventory.setItem(9, addBalanceAll)

        inventory.setItem(13, wallet)
        inventory.setItem(22, confirmItem)

        inventory.setItem(14, getBalance1)
        inventory.setItem(15, getBalance16)
        inventory.setItem(16, getBalance64)
        //inventory.setItem(17, getBalanceAll)
        return inventory
    }

//    fun updateWalletItem(player: Player, inventory: Inventory) {
//        val uuid = player.uniqueId.toString()
//        val walletD = userDB.getDefaultWalletByUUID(uuid) ?: 0
//        val currency = walletDB.getWalletCurrency(walletD) ?: "[Missing currency]"
//        val balance = walletDB.getWalletBalance(walletD) ?: "[Missing balance]"
//        val dateReg = walletDB.getVerificationWalletDate(walletD) ?: "[Missing dateReg]"
//
//        // Кнопка кошелька
//        val walletPrivate = systemGUI.createItem(
//            Material.PAPER,
//            localizationManager.getMessage("localisation.inventory.item.wallet"),
//            listOf(localizationManager.getMessage("localisation.inventory.lore.wallet.actions-menu",
//                "owner" to player.name,
//                "balance" to balance.toString(),
//                "date" to dateReg)),
//            1
//        )
//
//        inventory.setItem(13, walletPrivate)
//    }

    fun updateItem(player: Player, inventory: Inventory) {
        val transferData = actionData.getActionData(player) ?: return
        val amount = transferData.amount
        val type = when{
            amount > 0 -> "Пополнение" //todo: ЛОКАЛИЗАЦИЮ
            amount < 0 -> "Снятие"
            else -> "Ожидание"
        }
        // Обновите предмет с названием выбранной головы в центре
        val centerItem = createWalletItem(type, transferData.amount)
        inventory.setItem(13, centerItem)
    }

    private fun createWalletItem(type: String, amount: Int): ItemStack {
        return systemGUI.createItem(
            Material.PAPER,
            type,
            listOf("$amount"),
            4
        )
    }
}