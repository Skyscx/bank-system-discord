package bank.commands

import data.Database
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TransferCommand(private val database: Database) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.size != 3) return false

        val uuid = sender.uniqueId.toString()
        val isAdmin = sender.hasPermission("skybank.banker") // Проверить права
        var amount = 0
        var senderWalletID: Int? = null
        var targetWalletID: Int? = null

        val senderIdentifier = args[0]
        val targetIdentifier = args[1]
        val amountArg = args[2].toIntOrNull() ?: return false

        senderWalletID = database.getWalletID(senderIdentifier, uuid)
        targetWalletID = database.getWalletID(targetIdentifier, uuid)

        if (senderWalletID == null || targetWalletID == null) {
            sender.sendMessage("Кошелек не найден.")
            return false
        }

        if (database.doesIdExistAccount(targetWalletID) && database.doesIdExistAccount(senderWalletID)) {
            val target = database.getPlayerByWalletID(targetWalletID)
            val currency1 = database.getWalletCurrency(senderWalletID) ?: return false
            val currency2 = database.getWalletCurrency(targetWalletID) ?: return false

            if (currency1 == currency2) {
                database.transferFunds(sender, target!!, senderWalletID, targetWalletID, amountArg, currency1, 1)
                sender.sendMessage("Операция выполнена!")
            } else {
                sender.sendMessage("Валюты кошельков не совпадают.")
            }
        } else {
            sender.sendMessage("Кошелек не найден.")
        }

        return true
    }
}