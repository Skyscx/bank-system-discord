package bank.commands.accounts.collection

import org.bukkit.command.CommandSender

class RenameCommandHandler {
    fun handleRenameCommand(sender: CommandSender, args: Array<String>) {
        sender.sendMessage("Handling rename command...")
    }
}