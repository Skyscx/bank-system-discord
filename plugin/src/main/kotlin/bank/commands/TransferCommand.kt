package bank.commands

import data.Database
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TransferCommand(private val database: Database) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.isEmpty()) return false
        val uuid = sender.uniqueId.toString()
        var senderWalletID: Int? = null
        var targetWalletID: Int? = null
        var amount: Int? = null
        when(args[0].lowercase()){
            // dm - sender = default || target = args(ID/NAME WALLET)
            "dm" ->{
                senderWalletID = database.getDefaultWalletIDByUUID(uuid)
                targetWalletID = database.getWalletID(args[1])
                if (targetWalletID == null){
                    sender.sendMessage("Кошелек не найден.")
                    return true
                }
                amount = args[2].toIntOrNull() ?: return false
            }
            // dd - sender = default || target = default
            "dd" ->{
                if (args.size != 4) return false
                senderWalletID = database.getDefaultWalletIDByUUID(uuid)
                if (senderWalletID == null){
                    sender.sendMessage("У вас не установлен основной кошелек.")
                    return true
                }
                amount = args[3].toIntOrNull() ?: return false
                when(args[1].lowercase()){
                    "player" ->{
                        val playerName = args[2]
                        val player = Bukkit.getPlayer(playerName)
                        if (player == null){
                            sender.sendMessage("Игрок с именем $playerName не найден.")
                            return true
                        }
                        val uuidTarget = player.uniqueId.toString()
                        if (!database.doesUUIDWalletsExist(uuidTarget)){
                            sender.sendMessage("Игрок получатель не существует в банковской системе.")
                            return true
                        }
                        targetWalletID = database.getDefaultWalletIDByUUID(uuidTarget)
                        if (targetWalletID == null) {
                            sender.sendMessage("У игрока не установлен основной кошелек.")
                            return true
                        }

                    }
                    "uuid" ->{
                        val uuidTarget = args[2]
                        if (!database.doesUUIDWalletsExist(uuidTarget)){
                            sender.sendMessage("Игрок получатель не существует в банковской системе.")
                            return true
                        }
                        targetWalletID = database.getDefaultWalletIDByUUID(uuidTarget)
                        if (targetWalletID == null) {
                            sender.sendMessage("У игрока не установлен основной кошелек.")
                            return true
                        }
                    }
                    "discordID" ->{
                        val discordIDTarget = args[2]
                        database.getUUIDbyDiscordID(discordIDTarget).thenAccept { uuidTarget ->
                            if (uuidTarget == null) {
                                sender.sendMessage("Discord ID не был найден в банковской системе.")
                                println("UUID для Discord ID $discordIDTarget не найден.")
                            } else {
                                //sender.sendMessage("Discord ID был найден в банковской системе.")
                                targetWalletID = database.getDefaultWalletIDByUUID(uuidTarget)
                                println("UUID для Discord ID $discordIDTarget: $uuidTarget")
                            }
                        }.exceptionally { e ->
                            e.printStackTrace()
                            sender.sendMessage("Ошибка поиска пользователя в банковской системе.")
                            println("Произошла ошибка при поиске UUID для Discord ID $discordIDTarget.")
                            null
                        }
                    } else ->{
                        sender.sendMessage("Неверные аргументы!")
                        return false
                    }
                }
            }
            // md - sender = args(ID/NAME WALLET) || target = default
            "md" ->{
                if (args.size != 5) return false
                senderWalletID = database.getWalletID(args[1])
                if (senderWalletID == null){
                    sender.sendMessage("Номер кошелька не найден.")
                    return true
                }
                amount = args[4].toIntOrNull() ?: return false
                when(args[2].lowercase()){
                    "player" ->{
                        val playerName = args[3]
                        val player = Bukkit.getPlayer(playerName)
                        if (player == null) {
                            sender.sendMessage("Игрок с именем $playerName не найден.")
                            return true
                        }
                        val uuidTarget = player.uniqueId.toString()
                        if (!database.doesUUIDWalletsExist(uuidTarget)){
                            sender.sendMessage("Игрок получатель не существует в банковской системе.")
                            return true
                        }
                        targetWalletID = database.getDefaultWalletIDByUUID(uuidTarget)
                        if (targetWalletID == null){
                            sender.sendMessage("Кошелек по умолчанию не найден у игрока.")
                            return true
                        }
                    }
                    "uuid" ->{
                        val uuidTarget = args[3]
                        if (!database.doesUUIDWalletsExist(uuidTarget)){
                            sender.sendMessage("Игрок получатель не существует в банковской системе.")
                            return true
                        }
                        targetWalletID = database.getDefaultWalletIDByUUID(uuidTarget)
                        if (targetWalletID == null) {
                            sender.sendMessage("У игрока не установлен основной кошелек.")
                            return true
                        }

                    }
                    "discordID" ->{
                        val discordIDTarget = args[3]
                        database.getUUIDbyDiscordID(discordIDTarget).thenAccept { uuidTarget ->
                            if (uuidTarget == null) {
                                sender.sendMessage("Discord ID не был найден в банковской системе.")
                                println("UUID для Discord ID $discordIDTarget не найден.")
                            } else {
                                //sender.sendMessage("Discord ID был найден в банковской системе.")
                                targetWalletID = database.getDefaultWalletIDByUUID(uuidTarget)
                                println("UUID для Discord ID $discordIDTarget: $uuidTarget")
                            }
                        }.exceptionally { e ->
                            e.printStackTrace()
                            sender.sendMessage("Ошибка поиска пользователя в банковской системе.")
                            println("Произошла ошибка при поиске UUID для Discord ID $discordIDTarget.")
                            null
                        }
                    } else ->{
                        sender.sendMessage("Неверные аргументы!")
                        return false
                    }
                }
            }
            // sender = args(ID/NAME WALLET) || target = args(ID/NAME WALLET)
            else ->{
                senderWalletID = database.getWalletID(args[0])
                targetWalletID = database.getWalletID(args[1])
                amount = args[3].toIntOrNull() ?: return false
            }
        }
        val uuidWalletSender = database.getUUID(senderWalletID!!)
        if (uuidWalletSender != uuid){
            sender.sendMessage("Вы не владелец этого кошелька!")
            return true
        }
        if (database.doesIdExistWallet(targetWalletID!!) && database.doesIdExistWallet(senderWalletID)) {
            val target = database.getPlayerByWalletID(targetWalletID!!)
            val currency1 = database.getWalletCurrency(senderWalletID) ?: return false
            val currency2 = database.getWalletCurrency(targetWalletID!!) ?: return false

            if (currency1 == currency2) {
                val bool = database.transferFunds(sender, target!!, senderWalletID, targetWalletID!!, amount, currency1, 1)
                if (bool) {
                    sender.sendMessage("Операция выполнена")
                }else{
                    sender.sendMessage("не выполнена операция")
                }
            //sender.sendMessage("Операция выполнена!")
            } else {
                sender.sendMessage("Валюты кошельков не совпадают.")
            }
        } else {
            sender.sendMessage("Кошелек не найден.")
        }

        //if (args.size != 3) return false

        //val isAdmin = sender.hasPermission("skybank.banker") // Проверить права
        //var amount = 0

        //var senderWalletID: Int? = null
        //var targetWalletID: Int? = null

        //val senderIdentifier = args[0]
        //val targetIdentifier = args[1]
        //val amountArg = args[2].toIntOrNull() ?: return false

        //senderWalletID = database.getWalletID(senderIdentifier)
        //targetWalletID = database.getWalletID(targetIdentifier)

//        if (senderWalletID == null || targetWalletID == null) {
//            sender.sendMessage("Кошелек не найден.")
//            return false
//        }
//        val uuidWalletSender = database.getUUID(senderWalletID)
//        if (uuidWalletSender != uuid){
//            sender.sendMessage("Вы не владелец этого кошелька!")
//            return true
//
//        }



        return true
    }
}