package bank.commands.accounts.collection

import org.bukkit.command.CommandSender

class ListCommandHandler {
    fun handleListCommand(sender: CommandSender, args: Array<String>) {
        sender.sendMessage("Handling list command...")
    }
}