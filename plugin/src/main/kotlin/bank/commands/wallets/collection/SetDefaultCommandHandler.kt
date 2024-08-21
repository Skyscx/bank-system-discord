package bank.commands.wallets.collection

import App.Companion.localizationManager
import App.Companion.walletDB
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetDefaultCommandHandler() {
    val functions = Functions()
    fun handleSetDefaultCommand(sender: CommandSender, args: Array<String>) {
        if (functions.checkArguments(sender, 2, args, localizationManager.getMessage("localisation.messages.usage.account.remove.all"))) return
        val identifier = args[1]
        val walletID = identifier.toIntOrNull() ?: walletDB.getWalletID(identifier)
        val player = sender as Player
        val uuid = player.uniqueId.toString()
        if (walletID == null || !walletDB.doesIdExistWallet(walletID)) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null"))
            return
        }
        val walletOwnerUUID = walletDB.getUUIDbyWalletID(walletID)
        if (uuid != walletOwnerUUID) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
            return
        }
        walletDB.setDefaultWalletID(uuid, walletID)
        return
    }
}