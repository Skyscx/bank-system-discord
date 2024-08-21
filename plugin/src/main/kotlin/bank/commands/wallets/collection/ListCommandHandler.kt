package bank.commands.wallets.collection

import App.Companion.localizationManager
import App.Companion.walletDB
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ListCommandHandler() {
    fun handleListCommand(sender: CommandSender) {
        val player = sender as Player
        val uuid = player.uniqueId.toString()
        val walletsPlayer = walletDB.getIdsWalletsOwnerByUUID(uuid)
        for (id in walletsPlayer){
            val walletData = walletDB.getWalletDataByID(id)
            if (walletData == null) {
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.error-search-data-in-database"))
                return
            }
            sender.sendMessage(walletData)
        }
        return
    }
}