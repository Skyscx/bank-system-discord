package bank.commands.accounts.collection

import App.Companion.localizationManager
import functions.Functions
import org.bukkit.command.CommandSender

class RemoveCommandHandler {
    val functions = Functions()
    fun handleRemoveCommand(sender: CommandSender, args: Array<String>) {
        if (args.size <= 1){
            //TODO: Если кошельки существуют - то открытие GUI
            //TODO: Если кошельки не существуют - то вывод сообщения
        }
        when(args[1].lowercase()){
            "id" -> {
                if (functions.checkArguments(sender, 4, args, localizationManager.getMessage("localisation.messages.usage.account.remove.id"))) return

            }
            "name" -> {}
            "all" -> {}
        }

    }
}