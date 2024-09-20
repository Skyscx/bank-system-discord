package bank.commands.wallets.collection

import App.Companion.configPlugin
import App.Companion.historyDB
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import functions.Functions
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AddBalanceCommandHandler() {
    //todo: wallet balance add/remove [amount]
    val functions = Functions()
    fun handleAddBalanceCommand(sender: CommandSender, args: Array<String>) {
        if (!functions.checkArguments(sender, 3, args, "localisation.messages.usage.account.balance.add".localized())) return
        val amount = args[2].toIntOrNull()
        if (amount == null) {
            sender.sendMessage("localisation.error.not-integer".localized())
            return
        }
        if (amount <= 0) {
            sender.sendMessage("localisation.messages.out.wallet.balance.not-plus".localized())
            return
        }
        val player = sender as Player
        val uuid = userDB.getUUIDbyPlayerName(player.name) ?: return
        val walletDefault = userDB.getDefaultWalletByUUID(uuid) ?: return
        val walletVerification = walletDB.getVerificationWallet(walletDefault)
        if (walletVerification != 1){
            sender.sendMessage("localisation.messages.out.wallet.unavailable.sender".localized())
            return
        }
        val walletCurrency = walletDB.getWalletCurrency(walletDefault) ?: return
        val currency = functions.convertStringToMaterial(walletCurrency)
        val typeBlock : Material
        if (currency.second) {
            typeBlock = currency.first!!
        } else {
            sender.sendMessage("localisation.error.init_currency".localized())
            return
        }

        val limit = configPlugin.getInt("wallet-limit")
        val balance = walletDB.getWalletBalance(walletDefault) ?: 0
        if (balance + amount > limit){
            val free = (balance + amount - limit).toString()
            sender.sendMessage("localisation.messages.out.wallet.balance.overflow".localized("free" to free))
            return
        }
        val countPlayerBlock = functions.countBlocksInInventory(player, typeBlock)
        if (amount > countPlayerBlock) {
            sender.sendMessage("localisation.error.not-player-blocks".localized())
            return
        }
        functions.takeItem(player, typeBlock, amount)
        walletDB.updateWalletBalance(walletDefault, amount)
        historyDB.insertBankHistory(
            typeOperation = "ADD_BALANCE",
            senderName = sender.name,
            senderWalletID = walletDefault,
            uuidSender = uuid,
            amount = amount,
            currency = typeBlock.name,
            status = 1,
            oldBalance = balance,
            newBalance = balance + amount,

            uuidTarget = "null",
            comment =  "null",
            targetWalletID = 0 ,
            targetName = "null"
        )
        return
    }

}