package bank.commands.accounts.collection

import org.bukkit.command.CommandSender

class SetNameCommandHandler {
    fun handleSetNameCommand(sender: CommandSender, args: Array<String>) {
        sender.sendMessage("Handling set name command...")
    }
}