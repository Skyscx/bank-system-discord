package bank.commands.accounts.collection

import App.Companion.localizationManager
import data.Database
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RenameCommandHandler(private val database: Database) {
    val functions = Functions()

    fun handleRenameCommand(sender: CommandSender, args: Array<String>) {
        if (functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.rename"))) return

        val identifier = args[1]
        val newName = args[2]

        if (!functions.isWalletNameValid(newName)) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-name-valid"))
            return
        }

        val player = sender as Player
        val uuid = player.uniqueId.toString()
        val walletID = identifier.toIntOrNull() ?: database.getWalletID(identifier)

        if (walletID == null || !database.doesIdExistWallet(walletID)) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null"))
            return
        }
        val walletOwnerUUID = database.getUUIDbyWalletID(walletID)
        if (uuid != walletOwnerUUID) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
            return
        }

        if (!database.isWalletNameAvailable(newName)) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.name-is-not-free", "walletName" to newName))
            return
        }

        database.setNameWalletByIDWallet(newName, walletID)
    }
}