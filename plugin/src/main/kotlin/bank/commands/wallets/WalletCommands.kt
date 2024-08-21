package bank.commands.wallets

import App.Companion.localizationManager
import bank.commands.wallets.collection.*
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class WalletCommands : CommandExecutor{
    private val functions = Functions()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val isPlayer = functions.senderIsPlayer(sender)
        if (!isPlayer.second) {
            sender.sendMessage(isPlayer.first)
            return true
        }
        if (args.isEmpty()){
            //TODO: Сделать открытие меню инвентаря со всеми доступными функциями.
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.developing"))
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
            "set-default" -> {
                val setDefaultCommandHandler = SetDefaultCommandHandler()
                setDefaultCommandHandler.handleSetDefaultCommand(sender, argsArray)
            }
            "list" -> {
                val listCommandHandler = ListCommandHandler()
                listCommandHandler.handleListCommand(sender)
            }
            else -> {
                functions.unknownCommand(sender)
            }
        }
        return true
    }

}