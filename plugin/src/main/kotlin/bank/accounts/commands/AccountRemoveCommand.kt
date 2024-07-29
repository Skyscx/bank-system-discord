package bank.accounts.commands

import data.Database
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountRemoveCommand(private val database: Database) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.isEmpty()) return false
        when(args[0].lowercase()){
            // Удаление кошелька по ID
            "id" -> {

            }
            // Удаление кошелька по имени
            "name" -> {

            }
            // Удаление всех кошельков
            "all" -> {

            }
            // Принудительное удаление кошелька (Админ/Банкир команда)
            "force" -> {
                when(args[1].lowercase()){
                    // Принудительное удаление кошелька по ID
                    "id" ->{

                    }
                    // Принудительное удаление кошелька по имени
                    "name" ->{

                    }
                    // Принудительное удаление всех кошельков
                    "all" ->{
                        when(args[2].lowercase()){
                            // Принудительное удаление всех кошельков по UUID
                            "uuid" ->{

                            }
                            // Принудительное удаление всех кошельков по DiscordID
                            "discordID" -> {

                            }
                            // Принудительное удаление всех кошельков созданных в таблице кошельков
                            "server" ->{

                            }
                        }
                    }
                }
            }
            else -> {
                return false //TODO: Можно реализовать открытие GUI
            }
        }
        return true

//
//        if (sender !is Player) {
//            sender.sendMessage("Эту команду можно использовать только в игре.")
//            return true
//        }
//        if (args.size > 2 || args.size <= 1) return false
//        val id = args[0].toInt()
//        val bool = args[1].toBoolean()
//
//        //TODO: СДЕЛАТЬ ПРОВЕРКУ УДАЛЕНИЯ СЧЕТА - ЕСЛИ ВЛАДЕЛЦ - ТО ОК, ЕСЛИ НЕ ВЛАДЕЛЕЦ - ТО ТОЛЬКО ИГРОК С ПРАВОМ МОЖЕТ УДАЛИТЬ
//
//        if (bool){
//            database.deleteUserAccount(id)
//        }
//        return true
    }

}