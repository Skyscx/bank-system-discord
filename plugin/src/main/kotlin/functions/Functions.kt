package functions

import App.Companion.localizationManager
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
    fun takeItem(player: Player, itemType: Material, amount: Int) {

        val inventory = player.inventory
        var count = 0
        for (item in inventory.contents) {
            if (item != null && item.type == itemType) {
                count += item.amount
                if (count >= amount) {
                    inventory.removeItem(ItemStack(itemType, amount))
                    player.sendMessage("Вы потратили $amount x ${itemType.name}.") //todo: сделать сообщение из конфига
                    return
                }
            }
        }

        if (count < amount) {
            player.sendMessage("У вас недостаточно ${itemType.name}.") //todo: сделать сообщение из конфига
        }
    }
    fun convertStringToMaterial(itemType: String?): Pair<Material?, Boolean> {
        if (itemType == null) {
            return Pair(null, false)
        }

        return try {
            val material = Material.valueOf(itemType.uppercase())
            Pair(material, true)
        } catch (e: IllegalArgumentException) {
            Pair(null, false)
        }
    }


    fun countBlocksInInventory(player: Player, blockType: Material): Int {
        var count = 0
        val inventory = player.inventory
        for (item in inventory.contents) {
            if (item != null && item.type == blockType) {
                count += item.amount
            }
        }
        return count
    }
    fun giveItem(player: Player, item: ItemStack, amount: Int): Boolean {
        val inventory = player.inventory
        val freeSpace = inventory.firstEmpty()
        if (freeSpace == -1) {
            player.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.back-deposit.full-inventory"))
            return false
        }
        val newItem = item.clone()
        newItem.amount = amount
        inventory.addItem(newItem)
        val message = localizationManager.getMessage("localisation.messages.out.wallet.back-deposit",
            "amount" to amount.toString(), "currency" to item.type.name)
        println(message)
        player.sendMessage(message)
        return true
    }

    fun checkArguments(sender: CommandSender, expectedArgs: Int, args: Array<out String>, errorMessage: String): Boolean {
        if (args.size != expectedArgs) {
            sender.sendMessage(errorMessage)
            return false
        }
        return true
    }
    fun hasPermission(sender: CommandSender, permission: String): Boolean {
        return sender.hasPermission(permission) || sender.isOp
    }



}