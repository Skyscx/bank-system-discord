package gui.wallletmenu.actionwallet

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class WalletActionsInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    override fun createInventory(player: Player): Inventory {
        //todo: сделать получение цены создания кошелька.
        val title = Component.text(localizationManager.getMessage("localisation.inventory.title.menu-actions-wallet"))
        val inventory = Bukkit.createInventory(null, 27, title)
        val uuid = player.uniqueId.toString()
        val walletD = userDB.getDefaultWalletByUUID(uuid) ?: 0
        val currency = walletDB.getWalletCurrency(walletD) ?: "[Missing currency]"
        val balance = walletDB.getWalletBalance(walletD) ?: "[Missing balance]"
        val dateReg = walletDB.getVerificationWalletDate(walletD) ?: "[Missing dateReg]"
        // Кнопка пополнения 1
        val addBalance1 = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+1",
            listOf(localizationManager.getMessage("localisation.inventory.lore.add-balance.actions-menu",
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
            listOf(localizationManager.getMessage("localisation.inventory.lore.add-balance.actions-menu",
                "amount" to "64", "currencyName" to currency)),
            3
        )
        // Кнопка пополнения All
        val addBalanceAll = systemGUI.createItem(
            Material.LIME_WOOL,
            "§a+ALL",
            listOf(localizationManager.getMessage("localisation.inventory.lore.add-balance.actions-menu",
                "amount" to "[DEV]", "currencyName" to "[DEV]")),
            4
        )
        // Кнопка снятия 1
        val getBalance1 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-1",
            listOf(localizationManager.getMessage("localisation.inventory.lore.get-balance.actions-menu",
                "amount" to "1", "currencyName" to currency)),
            1
        )
        // Кнопка снятия 16
        val getBalance16 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-16",
            listOf(localizationManager.getMessage("localisation.inventory.lore.get-balance.actions-menu",
                "amount" to "16", "currencyName" to currency)),
            2
        )
        // Кнопка снятия 64
        val getBalance64 = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-64",
            listOf(localizationManager.getMessage("localisation.inventory.lore.get-balance.actions-menu",
                "amount" to "64", "currencyName" to currency)),
            3
        )
        // Кнопка снятия All
        val getBalanceAll = systemGUI.createItem(
            Material.ORANGE_WOOL,
            "§4-ALL",
            listOf(localizationManager.getMessage("localisation.inventory.lore.get-balance.actions-menu",
                "amount" to "[DEV]", "currencyName" to "[DEV]")),
            4
        )
        // Кнопка кошелька
        val walletPrivate = systemGUI.createItem(
            Material.PAPER,
            localizationManager.getMessage("localisation.inventory.item.wallet"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wallet.actions-menu",
                "owner" to player.name,
                "balance" to balance.toString(),
                "date" to dateReg)),
            1
        )
        // Вернуться в меню
        val backMenu = systemGUI.createItem(
            Material.DARK_OAK_DOOR,
            localizationManager.getMessage("localisation.inventory.item.back-wallet-menu"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wallet.back-wallet-menu")),
            1
        )

        inventory.setItem(4, backMenu)

        inventory.setItem(12, addBalance1)
        inventory.setItem(11, addBalance16)
        inventory.setItem(10, addBalance64)
        inventory.setItem(9, addBalanceAll)

        inventory.setItem(13, walletPrivate)

        inventory.setItem(14, getBalance1)
        inventory.setItem(15, getBalance16)
        inventory.setItem(16, getBalance64)
        inventory.setItem(17, getBalanceAll)
        return inventory
    }

    fun updateWalletItem(player: Player, inventory: Inventory) {
        val uuid = player.uniqueId.toString()
        val walletD = userDB.getDefaultWalletByUUID(uuid) ?: 0
        val currency = walletDB.getWalletCurrency(walletD) ?: "[Missing currency]"
        val balance = walletDB.getWalletBalance(walletD) ?: "[Missing balance]"
        val dateReg = walletDB.getVerificationWalletDate(walletD) ?: "[Missing dateReg]"

        // Кнопка кошелька
        val walletPrivate = systemGUI.createItem(
            Material.PAPER,
            localizationManager.getMessage("localisation.inventory.item.wallet"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wallet.actions-menu",
                "owner" to player.name,
                "balance" to balance.toString(),
                "date" to dateReg)),
            1
        )

        inventory.setItem(13, walletPrivate)
    }
}