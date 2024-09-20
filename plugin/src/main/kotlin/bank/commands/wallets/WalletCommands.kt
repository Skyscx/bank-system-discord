package bank.commands.wallets

import bank.commands.wallets.collection.*
import functions.Functions
import gui.InventoryManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class WalletCommands(private val config: FileConfiguration) : CommandExecutor{
    private val functions = Functions()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val isPlayer = functions.senderIsPlayer(sender)
        if (!isPlayer.second) {
            sender.sendMessage(isPlayer.first)
            return true
        }
        if (args.isEmpty()){
            val inventoryManager = InventoryManager()
            val player = sender as Player
            inventoryManager.openInventory(player, "menu")
            return true
        }
        val argsArray = args.toList().toTypedArray()
        when (args[0].lowercase()){
            "open" -> {
                val openCommandHandler = OpenCommandHandler()
                openCommandHandler.handleOpenCommand(sender, argsArray)
            }
            "remove" -> {
                val removeCommandHandler = RemoveCommandHandler(config)
                removeCommandHandler.handleRemoveCommand(sender, argsArray)
            }
            "balance" -> {
                if (args.size == 1) {
                    val balanceCommandHandler = BalanceCommandHandler()
                    balanceCommandHandler.handleBalanceCommand(sender)
                } else {
                    when (args[1].lowercase()) {
                        "add" -> {
                            val balanceAddCommandHandler = AddBalanceCommandHandler()
                            balanceAddCommandHandler.handleAddBalanceCommand(sender, argsArray)
                        }
                        "remove" -> {
                            val balanceRemoveCommandHandler = RemoveBalanceCommandHandler()
                            balanceRemoveCommandHandler.handleRemoveBalanceCommand(sender, argsArray)
                        }
                        else -> {
                            functions.unknownCommand(sender)
                        }
                    }
                }
            }
            "history" -> {
                val historyCommandHandler = HistoryCommandHandler()
                historyCommandHandler.handleHistoryCommand(sender, argsArray)
            }
            //TODO: Репорт сделать как в TransferEvent -> AnvilGUI
            "report" -> {
                val reportCommandHandler = ReportCommandHandler(config)
                reportCommandHandler.handleReportCommand(sender, argsArray)
            }
//            "rename" -> {
//                val renameCommandHandler = RenameCommandHandler()
//                renameCommandHandler.handleRenameCommand(sender, argsArray)
//            }
//            "set-default" -> {
//                val setDefaultCommandHandler = SetDefaultCommandHandler()
//                setDefaultCommandHandler.handleSetDefaultCommand(sender, argsArray)
//            }
//            "list" -> {
//                val listCommandHandler = ListCommandHandler()
//                listCommandHandler.handleListCommand(sender)
//            }
            else -> {
                functions.unknownCommand(sender)
            }
        }
        return true
    }

}