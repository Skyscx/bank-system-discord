package bank.commands.tabcompleter

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class AccountsCommandCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.size == 1) {
            return if (sender.hasPermission("skyresourcepack.admin") || sender.isOp) {
                listOf(
                    "TODO"
                )
            } else {
                listOf(
                    "open",
                    "remove",
                    "rename",
                    "set-default",
                    "set-name",
                    "list"
                )
            }
        }
        if (args.size == 2) {
            if (args[0].equals("open", ignoreCase = true)) {
                return listOf("<name>")
            }
            return emptyList()
        }
        return null
    }
}