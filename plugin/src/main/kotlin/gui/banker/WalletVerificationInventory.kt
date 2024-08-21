package gui.banker

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WalletVerificationInventory {
    fun accountVerificationMenu(player: Player) {
        val accountVerification = Bukkit.createInventory(null, 54, "Верификация пользователя")
        //Head Info
        val headPlayer = ItemStack(Material.PLAYER_HEAD)  //Сделать скин головы у кого нужно верифицировать
        val metaHeadPlayer = headPlayer.itemMeta!!
        metaHeadPlayer.setDisplayName("Игрок $ name")
        metaHeadPlayer.lore = listOf("value 1 \n" +
                "value 2 \n" +
                "value 3")
        headPlayer.setItemMeta(metaHeadPlayer)
        //Accept
        val accept = ItemStack(Material.GREEN_WOOL)
        val metaAccept = accept.itemMeta!!
        metaAccept.setDisplayName("Верифицировать!")
        metaAccept.lore = listOf<String>("Верифицировав, вы откроете кошелек игроку в банке")
        accept.setItemMeta(metaAccept)
        //CloseMenu
        val close = ItemStack(Material.RED_WOOL)
        val metaClose = close.itemMeta!!
        metaClose.setDisplayName("Отклонить!")
        close.setItemMeta(metaClose)

        //Places
        accountVerification.setItem(0,headPlayer)
        accountVerification.setItem(1, accept)
        accountVerification.setItem(2, close)

        //Inventory
        player.openInventory(accountVerification)
    }
}