package functions

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class Functions {
    fun isPlayerOnline(uuid: String): Boolean {
        val player = Bukkit.getPlayer(UUID.fromString(uuid))
        return player != null && player.isOnline
    }
    fun senderIsPlayer(sender: CommandSender): Pair<String, Boolean>{
        return if (sender is Player) {
            Pair("", true)
        } else {
            Pair("localisation.messages.out.sender-is-not-player", false)
        }
    }
    fun sendMessageIsPlayerOnline(uuid: String, message: String){
        if (isPlayerOnline(uuid)){
            getPlayerByUUID(uuid)?.sendMessage(message)
        }
    }
    fun sendMessageIsPlayerHavePermission(uuid: String, permission: String, message: String){ //TODO: Переделать логику под список из тех кто может видеть сообщение
        if (isPlayerOnline(uuid)){
            val player = getPlayerByUUID(uuid)
            if (player!!.hasPermission(permission) || player.isOp){
                player.sendMessage(message)
            }
        }
    }
    fun getPlayerByUUID(uuid: String): Player? {
        val player = Bukkit.getPlayer(UUID.fromString(uuid))
        return player
    }
    fun sendMessagePlayer(player: Player, message: String){
        player.sendMessage(message)
    }
    fun unknownCommand(sender: CommandSender){
        sender.sendMessage("localisation.messages.out.unknown-command")
    }
    fun hasDiamondOre(player: Player): Boolean { //TODO:Переделать на метод ниже takeItem
        val inventory = player.inventory
        var count = 0
        for (item in inventory.contents) {
            if (item != null && item.type == Material.DIAMOND_ORE) {
                count += item.amount
                if (count >= 15) {
                    //inventory.removeItem(ItemStack(Material.DIAMOND_ORE, 15))
                    return true
                }
            }
        }
        return false
    }
    fun isNumber(arg: String?): Boolean {
        return arg?.toIntOrNull() != null
    }
    fun isWalletNameValid(walletName: String): Boolean {
        return walletName.length in 5..32 && walletName.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*\$"))
    }
    fun takeItem(player: Player, itemType: String?, amount: Int) {
        if (itemType == null) {
            player.sendMessage("Неверный тип предмета.")
            return
        }

        val material: Material
        try {
            material = Material.valueOf(itemType)
        } catch (e: IllegalArgumentException) {
            player.sendMessage("Неверный тип предмета.")
            return
        }

        val inventory = player.inventory
        var count = 0
        for (item in inventory.contents) {
            if (item != null && item.type == material) {
                count += item.amount
                if (count >= amount) {
                    inventory.removeItem(ItemStack(material, amount))
                    player.sendMessage("Вы потратили $amount x ${material.name}.")
                    return
                }
            }
        }

        if (count < amount) {
            player.sendMessage("У вас недостаточно ${material.name}.")
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