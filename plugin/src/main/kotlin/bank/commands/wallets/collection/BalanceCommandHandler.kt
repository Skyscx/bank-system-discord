package bank.commands.wallets.collection

import App.Companion.userDB
import App.Companion.walletDB
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BalanceCommandHandler {
    fun handleBalanceCommand(sender: CommandSender, args: Array<String>) {
        val player = sender as Player
        val uuid = userDB.getUUIDbyPlayerName(player.name)
        if (uuid == null) {
            sender.sendMessage("Вы не зарегистрированы в банке")
        } else {
            val id = userDB.getDefaultWalletByUUID(uuid) ?: return
            val balance = walletDB.getWalletBalance(id)
            sender.sendMessage("Ваш баланс: $balance")
        }
        return
    }
}