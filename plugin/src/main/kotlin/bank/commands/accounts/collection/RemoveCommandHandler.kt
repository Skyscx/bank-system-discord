package bank.commands.accounts.collection

import App.Companion.localizationManager
import App.Companion.walletDB
import functions.Functions
import gui.InventoryManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RemoveCommandHandler () {
    val functions = Functions()
    fun handleRemoveCommand(sender: CommandSender, args: Array<String>) {
        val player = sender as Player
        val uuid = player.uniqueId.toString()

        if (args.size == 1){
            val inventoryManager = InventoryManager()
            val wallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
            if (wallets.isNotEmpty()){
                inventoryManager.openInventory(player, "remove")
            } else {
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.empty-wallets-list"))
            }

        }

        when(args[1].lowercase()){
            "all" -> {
                if (functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.remove.all"))) return

                val bool = args[2].toBooleanStrictOrNull()
                val ownerWallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
                if (bool == null) {
                    sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.account.remove.all.boolean"))
                    return
                }
                if (bool){
                    for (id in ownerWallets){
                        walletDB.deleteUserWallet(id)
                    }
                } else return
            }
            else -> {
                if (functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.remove"))) return
                val identifier = args[1]
                val bool = args[2].toBooleanStrictOrNull()
                if (bool == null) {
                    sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.account.remove.boolean"))
                    return
                }

                //Удаление по ID или по имени
                if (identifier.toIntOrNull() != null){
                    val walletID = identifier.toInt()
                    val ownerWallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
                    if (!(walletDB.doesIdExistWallet(walletID))){
                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null"))
                        return
                    }
                    if (walletID in ownerWallets) {
                        if (bool) walletDB.deleteUserWallet(walletID) else return // TODO: Подумать о выводе что нужно именно true
                    } else {
                        if (bool) sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
                        else return
                    }
                } else {
                    val walletID = walletDB.getIDByWalletName(identifier)
                    if (walletID == null) {
                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.error-search-data-in-database"))
                        return
                    }
                    val ownerWallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
                    if (walletID in ownerWallets){
                        if (bool) walletDB.deleteUserWallet(walletID) else return
                    } else {
                        if (bool) sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
                        else return
                    }
                }
            }
        }
    }
}