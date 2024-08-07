package bank.commands.accounts

import gui.сonfirmations.OpenAccountInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountOpenCommand: CommandExecutor {
    private val openAccountInventory = OpenAccountInventory()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        val player = sender.player
        //openAccountInventory.openAccountMenu(player!!)
        return true
    }
}