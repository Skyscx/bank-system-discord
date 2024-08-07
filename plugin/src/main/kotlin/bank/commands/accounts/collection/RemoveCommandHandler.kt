package bank.commands.accounts.collection

import org.bukkit.command.CommandSender

class RemoveCommandHandler {
    fun handleRemoveCommand(sender: CommandSender, args: Array<String>) {
        sender.sendMessage("Handling remove command...")
    }
}