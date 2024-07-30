package bank.commands.banker

import data.Database
import discord.FunctionsDiscord
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountVerificationCommand(private val database: Database) : CommandExecutor {
    private val function = Functions()
    private val functionDiscord = FunctionsDiscord()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.isEmpty()) return false
        when(args[0].lowercase()){
            "list" -> {
                val list = database.getUnverifiedWallets()
                for (id in list){
                    val playerData = database.getPlayerDataByID(id.toInt())
                    sender.sendMessage(playerData ?: "Список пуст!")
                }
            }
            else -> {
                if (args.size != 2) return false
                val id = args[0].toIntOrNull() ?: return false
                val bool = args[1].toBooleanStrictOrNull() ?: return false
                if (!database.doesIdExistWallet(id)){
                    sender.sendMessage("Такого кошелька не существует!")
                    return true
                }
                val verification = database.getVerificationWallet(id)
                val inspector = functionDiscord.getPlayerDiscordID(sender.uniqueId).toString()
                when(verification){
                    0 ->{
                        if (bool){
                            database.setVerificationWallet(id,1)
                            sender.sendMessage("Вы открыли счет игроку \$information.")
                        } else {
                            database.setVerificationWallet(id,-1)
                            sender.sendMessage("Вы отклонили счет игроку \$information")
                        }
                        database.setInspectorWallet(id, inspector)
                        database.setVerificationWalletDate(id)
                    }
                    1 -> sender.sendMessage("Счет уже открыт!")
                    -1 -> sender.sendMessage("Счет уже отклонен!")
                    else -> sender.sendMessage("Ошибка! Обратитесь к администратору!")
                }
            }
        }
        return true
    }
}