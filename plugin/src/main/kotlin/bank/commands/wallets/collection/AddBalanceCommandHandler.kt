package bank.commands.wallets.collection

import App.Companion.configPlugin
import App.Companion.historyDB
import App.Companion.localizationManager
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
        if (!functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.balance.add"))) return
        val amount = args[2].toIntOrNull()
        if (amount == null) {
            sender.sendMessage("Введите число арг2")
            return
        }
        val player = sender as Player
        val uuid = userDB.getUUIDbyPlayerName(player.name)
        if (uuid == null) {
            sender.sendMessage("нет в бд такого игрока")
            return
        }
        val walletDefault = userDB.getDefaultWalletByUUID(uuid)
        if (walletDefault == null) {
            sender.sendMessage("Ошибка инициализации кошелька")
            return
        }
        val walletVerification = walletDB.getVerificationWallet(walletDefault)
        if (walletVerification != 1){
            sender.sendMessage("Ваш кошелек не активирован.")
            return
        }
        val walletCurrency = walletDB.getWalletCurrency(walletDefault)
        if (walletCurrency == null){
            sender.sendMessage("ошибка валюты")
            return
        }
        val currency = functions.convertStringToMaterial(walletCurrency)
        val typeBlock : Material
        if (currency.second) {
            typeBlock = currency.first!! //Преобразованный материал
        } else {
            sender.sendMessage("Ошибка инициализации валюты")
            return

        }

        val limit = configPlugin.getInt("wallet-limit")
        val balance = walletDB.getWalletBalance(walletDefault) ?: 0
        if (balance + amount > limit){
            val free = (balance + amount - limit).toString()
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.balance.overflow", "free" to free))
            return
        }
        val countPlayerBlock = functions.countBlocksInInventory(player, typeBlock)
        if (amount > countPlayerBlock) {
            sender.sendMessage("У вас нет столько блоков в инвентаре.")
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

            uuidTarget = "null",
            comment =  "null",
            targetWalletID = 0 ,
            targetName = "null"
        )
        return
    }

}