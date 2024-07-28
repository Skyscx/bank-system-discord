package bank.accounts.commands

import data.Database
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountSetNameCommand(private val database: Database) : CommandExecutor{
    private val function = Functions()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.size > 2 || args.size <=1) return false
        val name = args[0]
        val id = args[1]
        if (!(function.isNumber(id))) return false
        val player = sender.player
        database.setAccountName(player?.uniqueId.toString(), name, id)
        player?.sendMessage("Счет #$id был назван $name.")
        return true
    }
}