package gui.accountmenu.renamingaccount

import data.Database
import gui.SystemGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory


class RenamingAccountInventory(private val database: Database) {
    private val renameInventory: Inventory by lazy {
        Bukkit.createInventory(null, 54, "Account Renaming: $currentWalletName")
    }
    private var currentWalletName = ""
    private var walletID: Int = 0
    private var isInitialized = false


    //private lateinit var walletID: String
    private val systemGUI = SystemGUI()
    fun openRenameInventory(player: Player, id: Int) {
        this.walletID = id
        currentWalletName = database.getNameWalletByIDWallet(walletID)
        //renameInventory = Bukkit.createInventory(null, 54, "Account Renaming: $currentWalletName")

        // Настройка слотов с цифрами
        val numberSlots = listOf(0, 1, 2, 3, 4, 9, 10, 11, 12, 13)
        for (i in numberSlots.indices) {
            renameInventory.setItem(numberSlots[i], systemGUI.createItem(Material.PAPER, i.toString()))
        }

        // Настройка слотов с чертачками
        renameInventory.setItem(5, systemGUI.createItem(Material.PAPER, "-"))
        renameInventory.setItem(14, systemGUI.createItem(Material.PAPER, "_"))

        // Настройка слотов для никнейма
        renameInventory.setItem(18, systemGUI.createItem(Material.NAME_TAG, "Nickname"))
        renameInventory.setItem(19, systemGUI.createItem(Material.NAME_TAG, "Nickname"))

        // Настройка слотов с буквами английского алфавита
        val alphabetSlots = listOf(20, 21, 22, 23, 24, 27, 28, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 42, 45, 46, 47, 48, 49, 50, 51)
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        for (i in alphabetSlots.indices) {
            renameInventory.setItem(alphabetSlots[i], systemGUI.createItem(Material.PAPER, alphabet[i].toString()))
        }

        // Настройка слотов для wallet
        val walletSlots = listOf(6, 7, 8, 15, 16, 17)
        for (slot in walletSlots) {
            renameInventory.setItem(slot, systemGUI.createItem(Material.PAPER, database.getNameWalletByIDWallet(id)))
        }

        // Настройка слотов для удаления символа
        renameInventory.setItem(25, systemGUI.createItem(Material.BARRIER, "Delete"))
        renameInventory.setItem(26, systemGUI.createItem(Material.BARRIER, "Delete"))

        // Настройка слотов для выхода
        renameInventory.setItem(34, systemGUI.createItem(Material.REDSTONE_BLOCK, "Exit"))
        renameInventory.setItem(35, systemGUI.createItem(Material.REDSTONE_BLOCK, "Exit"))

        // Настройка слотов для сохранения
        renameInventory.setItem(43, systemGUI.createItem(Material.EMERALD_BLOCK, "Save"))
        renameInventory.setItem(44, systemGUI.createItem(Material.EMERALD_BLOCK, "Save"))

        // Настройка слотов для гайда
        renameInventory.setItem(52, systemGUI.createItem(Material.BOOK, "Guide"))
        renameInventory.setItem(53, systemGUI.createItem(Material.BOOK, "Guide"))

        player.openInventory(renameInventory)

        isInitialized = true

    }
    fun updateWalletName() {
        val walletSlots = listOf(6, 7, 8, 15, 16, 17)
        for (slot in walletSlots) {
            renameInventory.setItem(slot, systemGUI.createItem(Material.PAPER, currentWalletName))
        }
        // Обновление названия инвентаря
        renameInventory.viewers.forEach { viewer ->
            viewer.openInventory(Bukkit.createInventory(null, 54, "Account Renaming: $currentWalletName"))
        }
    }
    fun getCurrentWalletName(): String {
        return currentWalletName
    }

    fun setCurrentWalletName(name: String) {
        currentWalletName = name
    }
    fun getWalletID(): Int {
        return walletID
    }
    fun getRenameInventory(): Inventory {
        return renameInventory
    }
    fun isInitialized(): Boolean {
        return isInitialized
    }
}