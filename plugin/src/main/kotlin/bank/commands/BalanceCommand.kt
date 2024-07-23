package bank.commands

import database.Database
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BalanceCommand(private val database: Database) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        val uuid = sender.uniqueId
        val balance = database.getPlayerBalance(uuid)
        sender.sendMessage("Ваш баланс: $balance")
        return true
    }
}