package bank.commands.accounts.collection

import org.bukkit.command.CommandSender

class SetDefaultCommandHandler {
    fun handleSetDefaultCommand(sender: CommandSender, args: Array<String>) {
        sender.sendMessage("Handling set-default command...")
    }
}