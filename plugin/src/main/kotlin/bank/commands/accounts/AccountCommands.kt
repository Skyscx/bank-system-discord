package bank.commands.accounts

import bank.commands.accounts.collection.*
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class AccountCommands : CommandExecutor{
    private val functions = Functions()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val isPlayer = functions.senderIsPlayer(sender)
        if (!isPlayer.second) {
            sender.sendMessage(isPlayer.first)
            return true
        }
        if (args.isEmpty()){
            //TODO: open inventory manager
            sender.sendMessage("--Developing an interaction menu--")
            return true
        }
        val argsArray = args.toList().toTypedArray()
        when (args[0].lowercase()){
            "open" -> {
                val openCommandHandler = OpenCommandHandler()
                openCommandHandler.handleOpenCommand(sender, argsArray)
            }
            "remove" -> {
                val removeCommandHandler = RemoveCommandHandler()
                removeCommandHandler.handleRemoveCommand(sender, argsArray)
            }
            "rename" -> {
                val renameCommandHandler = RenameCommandHandler()
                renameCommandHandler.handleRenameCommand(sender, argsArray)
            }
            "set-name" -> {
                val setNameCommandHandler = SetNameCommandHandler()
                setNameCommandHandler.handleSetNameCommand(sender, argsArray)
            }
            "set-default" -> {
                val setDefaultCommandHandler = SetDefaultCommandHandler()
                setDefaultCommandHandler.handleSetDefaultCommand(sender, argsArray)
            }
            "list" -> {
                val listCommandHandler = ListCommandHandler()
                listCommandHandler.handleListCommand(sender, argsArray)
            }
            else -> {
                functions.unknownCommand(sender)
            }
        }
        return true
    }

}