package bank.commands

import github.scarsz.discordsrv.api.commands.SlashCommand
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PayCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        // TODO: Implement currency transfer logic here.

        sender.sendMessage("You transferred 0 currency to no one.")



        return true
    }

    @SlashCommand(path = "pay")
    fun sendPayMessage(event: SlashCommandEvent){

        event.hook.sendMessage(event.name + " перевел деньги пока никому");
    }
}