package bank.commands.accounts.collection

import App.Companion.localizationManager
import data.Database
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ListCommandHandler(private val database: Database) {
    fun handleListCommand(sender: CommandSender) {
        val player = sender as Player
        val uuid = player.uniqueId.toString()
        val walletsPlayer = database.getIdsWalletsOwnerByUUID(uuid)
        for (id in walletsPlayer){
            val walletData = database.getWalletDataByID(id)
            if (walletData == null) {
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.error-search-data-in-database"))
                return
            }
            sender.sendMessage(walletData)
        }
        return
    }
}