package bank.commands.wallets.collection

import App.Companion.configPlugin
import App.Companion.historyDB
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import functions.Functions
import org.bukkit.Material
import org.bukkit.entity.Player

class AddBalanceCommandHandler {
    //todo: wallet balance add/remove [amount]
    val functions = Functions()

    fun handleAddBalanceCommand(player: Player, args: Array<String>) {
        if (!functions.checkArguments(player, 3, args, "localisation.messages.usage.account.balance.add".localized())) return
        val amount = args[2].toIntOrNull()
        if (amount == null) {
            player.sendMessage("localisation.error.not-integer".localized())
            return
        }
        if (amount <= 0) {
            player.sendMessage("localisation.messages.out.wallet.balance.not-plus".localized())
            return
        }

        val playerUUID = player.uniqueId
        userDB.isPlayerExists(playerUUID).thenAccept { exists ->
            if (exists) {
                val walletDefault = userDB.getDefaultWalletByUUID(playerUUID.toString()) ?: return@thenAccept
                val walletVerification = walletDB.getVerificationWallet(walletDefault)
                if (walletVerification != 1) {
                    player.sendMessage("localisation.messages.out.wallet.unavailable.sender".localized())
                    return@thenAccept
                }
                val walletCurrency = walletDB.getWalletCurrency(walletDefault) ?: return@thenAccept
                val currency = functions.convertStringToMaterial(walletCurrency)
                val typeBlock: Material
                if (currency.second) {
                    typeBlock = currency.first!!
                } else {
                    player.sendMessage("localisation.error.init_currency".localized())
                    return@thenAccept
                }

                val limit = configPlugin.getInt("wallet-limit")
                val balance = walletDB.getWalletBalance(walletDefault) ?: 0
                if (balance + amount > limit) {
                    val free = (balance + amount - limit).toString()
                    player.sendMessage("localisation.messages.out.wallet.balance.overflow".localized("free" to free))
                    return@thenAccept
                }
                val countPlayerBlock = functions.countBlocksInInventory(player, typeBlock)
                if (amount > countPlayerBlock) {
                    player.sendMessage("localisation.error.not-player-blocks".localized())
                    return@thenAccept
                }
                functions.takeItem(player, typeBlock, amount)
                walletDB.updateWalletBalance(walletDefault, amount)
                historyDB.insertBankHistory(
                    typeOperation = "ADD_BALANCE",
                    senderName = player.name,
                    senderWalletID = walletDefault,
                    uuidSender = playerUUID.toString(),
                    amount = amount,
                    currency = typeBlock.name,
                    status = 1,
                    oldBalance = balance,
                    newBalance = balance + amount,
                    uuidTarget = "null",
                    comment = "null",
                    targetWalletID = 0,
                    targetName = "null"
                )
            } else {
                player.sendMessage("localisation.error.not-search-target".localized())
            }
        }.exceptionally { e ->
            e.printStackTrace()
            null
        }
    }
}