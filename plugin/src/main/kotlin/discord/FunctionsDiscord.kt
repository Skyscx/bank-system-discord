package discord

import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class FunctionsDiscord() {
    fun getPlayerDiscordID(uuid: UUID): String? {
        val discordId = DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid)
        return discordId
    }
    fun isPlayerOnline(uuid: String): Boolean {
        val player = Bukkit.getPlayer(UUID.fromString(uuid))
        return player != null && player.isOnline
    }
    fun getPlayerByUUID(uuid: String): Player? {
        val player = Bukkit.getPlayer(UUID.fromString(uuid))
        return player
    }
    fun sendMessagePlayer(player: Player, message: String){
        player.sendMessage(message)
    }
    fun hasDiamondOre(player: Player): Boolean {
        val inventory = player.inventory
        var count = 0
        for (item in inventory.contents) {
            if (item != null && item.type == Material.DIAMOND_ORE) {
                count += item.amount
                if (count >= 15) {
                    inventory.removeItem(ItemStack(Material.DIAMOND_ORE, 15))
                    return true
                }
            }
        }
        return false
    }
    fun isNumber(arg: String?): Boolean {
        return arg?.toIntOrNull() != null
    }
    fun takeItem(player: Player, itemType: Material, amount: Int) {
        val inventory = player.inventory
        var count = 0
        for (item in inventory.contents) {
            if (item != null && item.type == itemType) {
                count += item.amount
                if (count >= amount) {
                    inventory.removeItem(ItemStack(itemType, amount))
                    player.sendMessage("Вы потратили $amount x ${itemType.name}.")
                }else{
                    player.sendMessage("У вас недостаточно ${itemType.name}.")
                }
            }
        }
    }

    fun giveItem(player: Player, item: ItemStack, amount: Int) {
        val inventory = player.inventory
        val freeSpace = inventory.firstEmpty()

        if (freeSpace == -1) {
            player.sendMessage("У вас недостаточно места в инвентаре.")
            return
        }

        val newItem = item.clone()
        newItem.amount = amount

        inventory.addItem(newItem)
        player.sendMessage("Вы получили $amount x ${item.type.name}.")
    }


}