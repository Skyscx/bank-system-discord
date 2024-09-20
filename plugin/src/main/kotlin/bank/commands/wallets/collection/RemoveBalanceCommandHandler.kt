package bank.commands.wallets.collection

import App.Companion.historyDB
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import functions.Functions
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RemoveBalanceCommandHandler {
    //todo: wallet balance add/remove [amount]
    val functions = Functions()

    fun handleRemoveBalanceCommand(sender: CommandSender, args: Array<String>) {
        if (!functions.checkArguments(sender, 3, args, "localisation.messages.usage.account.balance.remove".localized())) return
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
        val typeBlock: Material
        if (currency.second) {
            typeBlock = currency.first!!
        } else return

        val balance = walletDB.getWalletBalance(walletDefault) ?: 0
        if (balance < amount) {
            sender.sendMessage("localisation.messages.out.wallet.not-balance".localized())
            return
        }

        val item = ItemStack(typeBlock)
        val successful = functions.giveItem(player, item, amount)
        if (successful) {
            walletDB.updateWalletBalance(walletDefault, -amount)
            historyDB.insertBankHistory(
                typeOperation = "GET_BALANCE",
                senderName = sender.name,
                senderWalletID = walletDefault,
                uuidSender = uuid,
                amount = amount,
                currency = typeBlock.name,
                status = 1,
                oldBalance = balance,
                newBalance = balance - amount,

                uuidTarget = "null",
                comment =  "null",
                targetWalletID = 0,
                targetName = "null"
            )
            sender.sendMessage("localisation.messages.out.wallet.balance.get".localized(
                "amount" to amount.toString(),
                "currency" to currency.first.toString()))
        } else {
            sender.sendMessage("localisation.messages.out.aborted".localized())
        }
    }


}