package bank.commands.wallets.collection

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class RemoveCommandHandler (config: FileConfiguration, discordBot: DiscordBot) {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(discordBot.getJDA(), config)
    //private val channelIdLogger = config.getString("channel-id-logger") ?: "null"


    fun handleRemoveCommand(sender: CommandSender, args: Array<String>) {
        val player = sender as Player
        val uuid = player.uniqueId.toString()

//        if (args.size == 1){ //todo: Реализовать позже!
//            val inventoryManager = InventoryManager()
//            val wallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
//            if (wallets.isNotEmpty()){
//                inventoryManager.openInventory(player, "remove")
//            } else {
//                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.empty-wallets-list"))
//            }
//
//        }

        if (!functions.checkArguments(sender, 2, args, localizationManager.getMessage("localisation.messages.usage.account.remove"))) return
        val bool = args[1].toBooleanStrictOrNull() ?: false
        if (!bool){
            sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.account.remove.boolean"))
            return
        }
        val walletID = userDB.getDefaultWalletByUUID(uuid)
        if (walletID == null) {
            sender.sendMessage("error")
            return
        }
        val balance = walletDB.getWalletBalance(walletID).toString()
        val currency = walletDB.getWalletCurrency(walletID).toString()
        val successful = walletDB.deleteUserWallet(walletID)
        if (successful){
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.remove-successfully.sender"))
            discordNotifier.sendMessageChannelLog(localizationManager.getMessage("localisation.discord.logger.remove-successfully", "player" to sender.name, "amount" to balance, "currency" to currency))
        } else {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.remove-unsuccessfully.sender"))
        }
        return



//        when(args[1].lowercase()){
//            "all" -> {
//                //todo: Реализовать позже!
//                if (functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.remove.all"))) return
//
//                val bool = args[2].toBooleanStrictOrNull()
//                val ownerWallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
//                if (bool == null) {
//                    sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.account.remove.all.boolean"))
//                    return
//                }
//                if (bool){
//                    for (id in ownerWallets){
//                        walletDB.deleteUserWallet(id)
//                    }
//                } else return
//                return
//            }
//            else -> {
//                if (!functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.remove"))) return
//                val identifier = args[1]
//                val bool = args[2].toBooleanStrictOrNull() ?: false
//                if (!bool) {
//                    sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.account.remove.boolean"))
//                    return
//                }
//
//                //Удаление по ID или по имени
//                if (identifier.toIntOrNull() != null){
//                    val walletID = identifier.toInt()
//                    val ownerWallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
//                    if (!(walletDB.doesIdExistWallet(walletID))){
//                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null", "wallet-owner" to "soon"))
//                        return
//                    }
//                    if (walletID in ownerWallets){
//                        walletDB.deleteUserWallet(walletID)
//                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.remove-successfully.sender"))
//                        return
//                    } else {
//                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
//                        return
//                    }
//                } else {
//                    val walletID = walletDB.getIDByWalletName(identifier)
//                    if (walletID == null) {
//                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.error-search-data-in-database"))
//                        return
//                    }
//                    val ownerWallets = walletDB.getIdsWalletsOwnerByUUID(uuid)
//                    if (walletID in ownerWallets){
//                        walletDB.deleteUserWallet(walletID)
//                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.remove-successfully.sender"))
//                        return
//                    } else {
//                        sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
//                        return
//                    }
//                }
//            }
//        }
    }
}