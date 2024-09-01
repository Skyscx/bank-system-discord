package bank.commands.tabcompleter

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class WalletsForceCommandCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.size == 1)  return listOf(
                "remove",
                "balance"
            )
        if (args.size == 2) {
            if (args[0] == "balance") return listOf(
                    "add",
                    "remove",
                )
            return emptyList()
        }
        return null
    }
}