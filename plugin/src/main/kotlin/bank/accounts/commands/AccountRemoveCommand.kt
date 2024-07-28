package bank.accounts.commands

import data.Database
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountRemoveCommand(private val database: Database) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.size > 2 || args.size <= 1) return false
        val id = args[0].toInt()
        val bool = args[1].toBoolean()

        //TODO: СДЕЛАТЬ ПРОВЕРКУ УДАЛЕНИЯ СЧЕТА - ЕСЛИ ВЛАДЕЛЦ - ТО ОК, ЕСЛИ НЕ ВЛАДЕЛЕЦ - ТО ТОЛЬКО ИГРОК С ПРАВОМ МОЖЕТ УДАЛИТЬ

        if (bool){
            database.deleteUserAccount(id)
        }
        return true
    }

}