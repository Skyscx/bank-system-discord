//package bank.commands.accounts
//
//import App.Companion.localizationManager
//import bank.commands.accounts.collection.ListCommandHandler
//import bank.commands.accounts.collection.RenameCommandHandler
//import bank.commands.accounts.collectionforce.RemoveForceCommandHandler
//import functions.Functions
//import org.bukkit.command.Command
//import org.bukkit.command.CommandExecutor
//import org.bukkit.command.CommandSender
//
//class AccountForceCommands : CommandExecutor {
//    private val functions = Functions()
//    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        val isPlayer = functions.senderIsPlayer(sender)
//        if (!isPlayer.second) {
//            sender.sendMessage(isPlayer.first)
//            return true
//        }
//        if (!(functions.hasPermission(sender, "skybank.banker") || sender.isOp)){
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.no-permissions"))
//            return true
//        }
//        if (args.isEmpty()){
//            //TODO: Сделать открытие меню инвентаря со всеми доступными функциями.
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.developing"))
//            return true
//        }
//        val argsArray = args.toList().toTypedArray()
//        when (args[0].lowercase()){
//            "remove" -> {
//                val removeForceCommandHandler = RemoveForceCommandHandler()
//                removeForceCommandHandler.handleRemoveForceCommand(sender, argsArray)
//            }
//            "rename" -> {
//                val renameCommandHandler = RenameCommandHandler()
//                renameCommandHandler.handleRenameCommand(sender, argsArray)
//            }
//            "set-name" -> {
//                val setNameCommandHandler = SetNameCommandHandler()
//                setNameCommandHandler.handleSetNameCommand(sender, argsArray)
//            }
//            "list" -> {
//                val listCommandHandler = ListCommandHandler()
//                listCommandHandler.handleListCommand(sender, argsArray)
//            }
//            "deactivate" -> {
//
//            }
//            else -> {
//                functions.unknownCommand(sender)
//            }
//        }
//        return true
//    }
//}