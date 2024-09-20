package bank.commands.wallets.collection

import App.Companion.historyDB
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class RemoveCommandHandler (config: FileConfiguration) {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

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

        if (!functions.checkArguments(sender, 3, args, "localisation.messages.usage.account.remove".localized())) return
        val bool = args[2].toBooleanStrictOrNull() ?: false
        if (!bool){
            sender.sendMessage("localisation.messages.usage.account.remove.boolean".localized())
            return
        }
        val walletID = userDB.getDefaultWalletByUUID(uuid) ?: return
        val balance = walletDB.getWalletBalance(walletID).toString()
        val currency = walletDB.getWalletCurrency(walletID).toString()
        val successful = walletDB.deleteUserWallet(walletID)
        if (successful){
            sender.sendMessage("localisation.messages.out.wallet.remove-successfully.sender".localized())
            historyDB.insertBankHistory(
                typeOperation = "CLOSE_WALLET",
                senderName = sender.name,
                senderWalletID = walletID,
                uuidSender = uuid.toString(),
                amount = 15, // todo: Сделать из ДБ
                currency = "DIAMOND_ORE", //todo: Сделать из дб
                status = 1,

                uuidTarget = "null",
                comment =  "null",
                targetWalletID = 0,
                targetName = "null"
            )
            discordNotifier.sendMessageChannelLog("localisation.discord.logger.remove-successfully".localized(
                "player" to sender.name,
                "amount" to balance,
                "currency" to currency
            ))
        } else {
            sender.sendMessage("localisation.messages.out.wallet.remove-unsuccessfully.sender".localized())
        }
        return
    }
}