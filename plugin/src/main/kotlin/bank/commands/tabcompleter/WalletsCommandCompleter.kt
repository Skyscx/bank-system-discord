package bank.commands.tabcompleter

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class WalletsCommandCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.size == 1)  return listOf(
                "open",
                "remove",
                "balance",
                "history"
//                "rename",
//                "set-default",
//                "list"
            )
        if (args.size == 2) {
            if (args[0] == "balance") return listOf(
                "add",
                "remove"
            )
//            if (args[0] == "remove") return listOf(
//                    " ",
//                    "<id/name>",
//                    "all"
//                )
//            if (args[0] == "rename") return listOf(
//                "<id/name>"
//            )
//            if (args[0] == "set-default") return listOf(
//                "<id/name>"
//            )
            return emptyList()
        }
//        if (args.size == 3){
//            if (args[0] == "remove") return listOf(
//                "<true/false>"
//            )
//            if (args[0] == "rename") return listOf(
//                "<new name>"
//            )
//        }
        return null
    }
}