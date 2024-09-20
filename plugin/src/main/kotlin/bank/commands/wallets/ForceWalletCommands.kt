package bank.commands.wallets

import App.Companion.localized
import bank.commands.wallets.collectionforce.RemoveForceCommandHandler
import bank.commands.wallets.collectionforce.RenamePlayerNameUser
import bank.commands.wallets.collectionforce.WalletBalanceAddForceHandler
import bank.commands.wallets.collectionforce.WalletBalanceRemoveForceHandler
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

class ForceWalletCommands(private val config: FileConfiguration) : CommandExecutor {
    //wallet-force
    private val functions = Functions()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val argsArray = args.toList().toTypedArray()
        if (!functions.hasPermission(sender, "skybank.banker")) { //TODO: Проверить права TESTING
            sender.sendMessage("localisation.messages.out.no-permissions".localized())
            return true
        }
        if (args.isEmpty()) {
            //TODO: Сделать открытие меню инвентаря со всеми доступными функциями.
            sender.sendMessage("localisation.messages.out.developing".localized())
            return true
        }
        when (args[0].lowercase()) {
            "remove" -> {
                val removeForceCommandHandler = RemoveForceCommandHandler(config)
                removeForceCommandHandler.handleRemoveForceCommand(sender, argsArray)
            }
            "balance" -> {
                when (args[1].lowercase()) {
                    "add" -> {
                        val walletBalanceAddForceHandler = WalletBalanceAddForceHandler(config)
                        walletBalanceAddForceHandler.handleBalanceAddForceCommand(sender, argsArray)
                    }
                    "remove" -> {
                        val walletBalanceRemoveForceHandler = WalletBalanceRemoveForceHandler(config)
                        walletBalanceRemoveForceHandler.handleBalanceRemoveForceCommand(sender, argsArray)
                    }
                    "rename" -> {
                        val renamePlayerNameUser = RenamePlayerNameUser(config)
                        renamePlayerNameUser.handleRenamePlayerNameUser(sender, argsArray)
                    }
                    else -> {
                        functions.unknownCommand(sender) //todo: переделать на сообщение о том что add или remove
                    }
                }
                return true
            }
            else -> {
                functions.unknownCommand(sender)
            }
        }
        return true
    }
}