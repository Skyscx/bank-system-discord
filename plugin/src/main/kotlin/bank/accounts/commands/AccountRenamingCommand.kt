package bank.accounts.commands

import data.Database
import gui.accountmenu.renamingaccount.RenamingAccountInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountRenamingCommand(private val database: Database) : CommandExecutor{
//    private val function = Functions()
    private val renamingAccountInventory = RenamingAccountInventory(database)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.size != 1) return false
        val id = args[0].toIntOrNull() ?: return false
//        val name = args[1]
//        if (!function.isWalletNameValid(name)){
//            sender.sendMessage("Имя кошелька должно быть минимум из 5 символов, максимум 32 символа. Первый символ не цифра.")
//            return true
//        }

        val uuidPlayerWallet = database.getUUID(id)
        if (uuidPlayerWallet != sender.uniqueId.toString()) {
            sender.sendMessage("Вы не владелец этого кошелька!")
            return true
        }
//        if (!database.isWalletNameAvailable(name)){
//            sender.sendMessage("Данное имя кошелька уже занято!")
//            return true
//        }
//        database.setWalletName(sender.uniqueId.toString(), name, id)
//        sender.sendMessage("Счет #$id был назван $name.")
        renamingAccountInventory.openRenameInventory(sender, id)
        return true
    }
}