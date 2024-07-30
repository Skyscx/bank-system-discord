package bank.accounts.commands

import data.Database
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountSetDefaultWalletCommand(private val database: Database): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.isEmpty()) return false
        when (args[0].lowercase()){
            // Установка основного кошелька по ID кошелька
            "id" ->{
                if (args.size != 2) return false
                val walletID = args[1].toInt()
                val uuid = sender.uniqueId.toString()
                database.setDefaultWalletID(uuid, walletID)
            }
            // Установка основного кошелька по Названию кошелька
            "name" ->{
                if (args.size != 2) return false
                val walletName = args[1]
                val walletID = database.getIDByWalletName(walletName)?: return false
                val uuid = sender.uniqueId.toString()
//                        if (walletID == null) {
//                            player.sendMessage("Кошелек '$walletName' не существует!")
//                            return true
//                        }
                if (database.doesIdExistWallet(walletID)){
                    database.setDefaultWalletID(uuid, walletID)
                } else {
                    sender.sendMessage("Кошелек '$walletName' не существует!")
                }
            }
        }

        return true
    }
}