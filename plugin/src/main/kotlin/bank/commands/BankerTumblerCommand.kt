package bank.commands

import App.Companion.configPlugin
import App.Companion.instance
import App.Companion.localizationManager
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class BankerTumblerCommand : CommandExecutor {
    private val functions = Functions()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!functions.hasPermission(sender, "skybank.admin")) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.no-permissions"))
            return true
        }

        if (!functions.checkArguments(sender, 1, args, localizationManager.getMessage("localisation.messages.usage.banker-tumbler"))) {
            return true
        }

        val bool = args[0].toBooleanStrictOrNull()
        if (bool == null) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.banker-tumbler"))
            return true
        }

        val boolCFG = configPlugin.getConfig().getBoolean("checker-banker")

        if (boolCFG == bool) {
            sender.sendMessage("Уже стоит")
        } else {
            configPlugin.getConfig().set("checker-banker", bool)
            instance.saveConfig()
            configPlugin.loadConfig()
            sender.sendMessage(if (bool) "Вы включили банкира" else "Вы выключили банкира")
        }

        return true
    }
}