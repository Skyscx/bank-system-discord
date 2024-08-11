package gui.accountmenu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class AccountsMenuInventory {
    //todo: old version
    fun openAccountMenu(player: Player) {
        val openAccount = Bukkit.createInventory(null, InventoryType.HOPPER, "Подтверждение операции")
        //Accept
        val accept = ItemStack(Material.GREEN_WOOL)
        val metaAccept = accept.itemMeta!!
        metaAccept.setDisplayName("Подтвердить!")
        metaAccept.lore = listOf<String>("Подтвердив операцию, с вашего инвентаря заберутся N алмазной руды.")
        accept.setItemMeta(metaAccept)
        openAccount.setItem(1, accept)
        //CloseMenu
        val close = ItemStack(Material.RED_WOOL)
        val metaClose = close.itemMeta!!
        metaClose.setDisplayName("Отклонить!")
        close.setItemMeta(metaClose)
        openAccount.setItem(3, close)

        player.openInventory(openAccount)
    }
}