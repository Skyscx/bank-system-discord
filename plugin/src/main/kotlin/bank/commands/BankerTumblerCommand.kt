package bank.commands

import App.Companion.configPlugin
import App.Companion.instance
import App.Companion.localized
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class BankerTumblerCommand : CommandExecutor {
    private val functions = Functions()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!functions.hasPermission(sender, "skybank.admin")) {
            sender.sendMessage("localisation.messages.out.no-permissions".localized())
            return true
        }

        if (!functions.checkArguments(sender, 1, args, "localisation.messages.usage.banker-tumbler".localized())) {
            return true
        }

        val bool = args[0].toBooleanStrictOrNull()
        if (bool == null) {
            sender.sendMessage("localisation.messages.usage.banker-tumbler".localized())
            return true
        }

        val boolCFG = configPlugin.getConfig().getBoolean("checker-banker")

        if (boolCFG == bool) {
            sender.sendMessage("localisation.messages.out.identically".localized())
            return true
        }
        configPlugin.getConfig().set("checker-banker", bool)
        instance.saveConfig()
        configPlugin.loadConfig()
        sender.sendMessage(
            if (bool) {
                "localisation.messages.out.banker.wallet.banker-tumbler.true".localized()
            } else {
                "localisation.messages.out.banker.wallet.banker-tumbler.false".localized()
            }
        )
        //todo: Пофиксить сразу применение
        return true
    }
}