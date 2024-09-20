package bank.commands.wallets.collection

import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BalanceCommandHandler {
    fun handleBalanceCommand(sender: CommandSender) {
        val player = sender as Player
        val uuid = userDB.getUUIDbyPlayerName(player.name) ?: return
        val id = userDB.getDefaultWalletByUUID(uuid) ?: return
        val balance = walletDB.getWalletBalance(id).toString()
        sender.sendMessage("localisation.messages.out.wallet.balance".localized("balance" to balance))
        return
    }
}