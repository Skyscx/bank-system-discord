package bank.commands.wallets.collection

import gui.InventoryManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpenCommandHandler {
    fun handleOpenCommand(sender: CommandSender, args: Array<String>) {
        val inventoryManager = InventoryManager()
        val player = sender as Player
        inventoryManager.openInventory(player, "open")
        //todo: добавить аргументы по желанию - название - валюта
    }
}