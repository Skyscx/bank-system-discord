package bank.commands.wallets.collection

import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import org.bukkit.entity.Player

class BalanceCommandHandler {
    fun handleBalanceCommand(player: Player) {
        val playerUUID = player.uniqueId
        userDB.isPlayerExists(playerUUID).thenAccept { exists ->
            if (exists) {
                val id = userDB.getDefaultWalletByUUID(playerUUID.toString()) ?: return@thenAccept
                val balance = walletDB.getWalletBalance(id).toString()
                player.sendMessage("localisation.messages.out.wallet.balance.show".localized("balance" to balance))
            } else {
                player.sendMessage("localisation.error.not-search-target".localized())
            }
        }.exceptionally { e ->
            e.printStackTrace()
            null
        }
    }
}