package bank.commands.banker

import database.Database
import discord.FunctionsDiscord
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountVerificationCommand(private val database: Database) : CommandExecutor {
    val functionsDiscord = FunctionsDiscord()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args[0] == "list"){
            val list = database.getUnverifiedAccounts()
            for (id in list) {
                val playerData = database.getPlayerDataById(id.toInt())
                sender.sendMessage(playerData!!)
            }
            return true
        }
        if (args.size > 2 || args.size <= 1) return false
        val id = args[0]
        if (!(functionsDiscord.isNumber(id))) return false
        val bool = args[1].toBoolean()
        val verif = database.getVerification(id.toInt())

        if (verif != -1) {
            if (bool && verif == 0) {
                database.setVerification(id.toInt(), 1)
                sender.sendMessage("Вы открыли")
            } else if (!bool && verif == 0) {
                database.setVerification(id.toInt(), -1)
                sender.sendMessage("Вы запретили")
            } else if (bool && verif == 1){
                sender.sendMessage("Уже открыли")
            }
        }else sender.sendMessage("Уже запретили")

        //val player = sender.player
        //database.updateVerification(id)

        //openAccountInventory.openAccountMenu(player!!)
        return true
    }
}