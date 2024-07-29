package gui.accountmenu.removeaccount

import gui.SystemGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class AccountRemoveInventory {
    private val systemGUI = SystemGUI()
    fun removeAccountMenu(player: Player) {
        val removeAccount = Bukkit.createInventory(null, 54, "Удаление кошелька")
        // default
        val removeMyAccountID = systemGUI.createItem(Material.APPLE, "Удалить мой кошелек.", listOf("Нажав, выйдет окно с вводом номера кошелька.\n После ввода необходимо будет подтвердить удаление"))
        val removeMyAccountName = systemGUI.createItem(Material.IRON_BLOCK, "Удалить мой кошелек", listOf("Нажав, выйдет окно с вводом названия кошелька.\n После ввода необходимо будет подтвердить удаление"))
        val removeMyAccountAll = systemGUI.createItem(Material.DRAGON_EGG, "Удалить все мои кошельки", listOf("Нажав, выйдет окно с подтверждение удаления всех кошельков. \n Будьте внимательны! Все ваши кошельки пропадут."))

        // forced
        val removeForcedAccountID = systemGUI.createItem(Material.DIAMOND_AXE, "Удалить чужой кошелек.", listOf("Нажав, выйдет окно с вводом номера кошелька, который вы хотите удалить. \n После ввода необходимо будет подтвердить удаление."))
        val removeForcedAccountName = systemGUI.createItem(Material.GOLDEN_AXE, "Удалить чужой кошелек.", listOf("Нажав, выйдет окно с вводом имени кошелька, который вы хотите удалить. \n После ввода необходимо будет подтвердить удаление."))

        // forced all
        val removeForcedAccountsUUID = systemGUI.createItem(Material.STONE_AXE, "Удалить все кошельки по UUID.", listOf("Нажав, выйдет окно с вводом UUID.\n После ввода необходимо будет подтвердить удаление."))
        val removeForcedAccountsDiscordID = systemGUI.createItem(Material.IRON_PICKAXE, "Удалить все кошельки по DiscordID.", listOf("Нажав, выйдет окно с вводом DiscordID, \n После необходимо будет подтвердить удаление."))
        val removeForcedAccountsTable = systemGUI.createItem(Material.BEDROCK, "Удалить все созданные на сервере кошельки.", listOf("Нажав, выйдет окно подтверждения удаления всех кошельков. \n Будьте Внимательны! Все кошельки на сервере пропадут!"))

        removeAccount.setItem(1, removeMyAccountID)
        removeAccount.setItem(2, removeMyAccountName)
        removeAccount.setItem(3, removeMyAccountAll)
        removeAccount.setItem(4, removeForcedAccountID)
        removeAccount.setItem(5, removeForcedAccountName)
        removeAccount.setItem(6, removeForcedAccountsUUID)
        removeAccount.setItem(7, removeForcedAccountsDiscordID)
        removeAccount.setItem(8, removeForcedAccountsTable)

        player.openInventory(removeAccount)
    }
}