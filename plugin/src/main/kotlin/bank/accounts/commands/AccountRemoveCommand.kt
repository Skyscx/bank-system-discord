package bank.accounts.commands

import data.Database
import gui.accountmenu.removeaccount.AccountRemoveInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountRemoveCommand(private val database: Database) : CommandExecutor {
    private val accountRemoveInventory = AccountRemoveInventory()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }
        if (args.isEmpty()) return false
        val uuid = sender.uniqueId.toString()
        val isBanker = sender.hasPermission("skybank.banker") //TODO: Проверить права
        when (args[0].lowercase()) {
            // Удаление кошелька по ID
            "id" -> {
                if (args.size != 3) return false
                val walletID = args[1].toIntOrNull() ?: return false
                val bool = args[2].toBoolean()
                val idOwner = database.getIdsWalletsOwnerByUUID(uuid)
                if (walletID in idOwner) {
                    if (bool) database.deleteUserWallet(walletID)
                    else return false
                } else {
                    if (bool) {
                        sender.sendMessage("Вы не владелец")
                    } else {
                        return false
                    }
                }
            }
            // Удаление кошелька по имени
            "name" -> {
                if (args.size != 3) return false
                val walletName = args[1]
                val bool = args[2].toBoolean()
                val walletID = database.getIDByWalletName(walletName) ?: return false
                val idOwner = database.getIdsWalletsOwnerByUUID(uuid)
                if (walletID in idOwner) {
                    if (bool) database.deleteUserWallet(walletID)
                    else return false
                } else {
                    if (bool) {
                        sender.sendMessage("Вы не владелец")
                    } else {
                        return false
                    }
                }
            }
            // Удаление всех кошельков
            "all" -> {
                if (args.size != 2) return false
                val bool = args[1].toBoolean()
                val idOwner = database.getIdsWalletsOwnerByUUID(uuid)
                if (bool) {
                    for (id in idOwner) {
                        database.deleteUserWallet(id)
                    }
                } else {
                    return false
                }
            }
            // Принудительное удаление кошелька (Админ/Банкир команда)
            "force" -> {
                if (!isBanker || !sender.isOp) {
                    sender.sendMessage("Нету прав!")
                }
                when (args[1].lowercase()) {
                    // Принудительное удаление кошелька по ID
                    "id" -> {
                        if (args.size != 4) return false
                        val walletID = args[2].toIntOrNull() ?: return false
                        val bool = args[3].toBoolean()
                        if (database.doesIdExistWallet(walletID)) {
                            if (bool) database.deleteUserWallet(walletID)
                            else return false
                        } else {
                            sender.sendMessage("Кошелек #$walletID не существует!")
                        }
                    }
                    // Принудительное удаление кошелька по имени
                    "name" -> {
                        if (args.size != 4) return false
                        val walletName = args[2]
                        val bool = args[3].toBoolean()
                        val walletID = database.getIDByWalletName(walletName) ?: return false
//                        if (walletID == null) {
//                            player.sendMessage("Кошелек '$walletName' не существует!")
//                            return true
//                        }
                        if (database.doesIdExistWallet(walletID)) {
                            if (bool) database.deleteUserWallet(walletID)
                            else return false
                        } else {
                            sender.sendMessage("Кошелек '$walletName' не существует!")
                        }
                    }
                    // Принудительное удаление всех кошельков
                    "all" -> {
                        if (args.size != 5) return false
                        val bool = args[4].toBoolean()
                        when (args[2].lowercase()) {
                            // Принудительное удаление всех кошельков по UUID
                            "uuid" -> {
                                val targetUUID = args[3]
                                val idOwnerList = database.getIdsWalletsOwnerByUUID(targetUUID)
                                if (bool) {
                                    for (id in idOwnerList) {
                                        database.deleteUserWallet(id)
                                    }
                                } else {
                                    return false
                                }
                            }
                            // Принудительное удаление всех кошельков по DiscordID
                            "discordID" -> {
                                val targetDiscordID = args[3]
                                val targetUUID = database.getUUIDbyDiscordID(targetDiscordID)
                                val idOwnerList = database.getIdsWalletsOwnerByUUID(targetUUID.toString())
                                if (bool) {
                                    for (id in idOwnerList) {
                                        database.deleteUserWallet(id)
                                    }
                                } else {
                                    return false
                                }
                            }
                            // Принудительное удаление всех кошельков созданных в таблице кошельков
                            "server" -> {
                                if (bool) {
                                    database.clearBankWalletsTable()
                                    sender.sendMessage("Вы удалили все кошельки на сервер")
                                } else {
                                    return false
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                accountRemoveInventory.removeAccountMenu(sender)
                return true //TODO: Можно реализовать открытие GUI
            }
        }
        return true

    }

}