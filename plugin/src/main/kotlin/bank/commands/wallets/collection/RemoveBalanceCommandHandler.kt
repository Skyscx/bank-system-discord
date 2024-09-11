package bank.commands.wallets.collection

import App.Companion.localizationManager
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
        if (!functions.checkArguments(sender, 3, args, localizationManager.getMessage("localisation.messages.usage.account.balance.remove"))) return
        val amount = args[2].toIntOrNull()
        if (amount == null) {
            sender.sendMessage("Введите число арг2")
            return
        }
        if (amount <= 0) {
            sender.sendMessage("Сумма должна быть положительным числом.")
            return
        }

        val player = sender as Player
        val uuid = userDB.getUUIDbyPlayerName(player.name) ?: return
        val walletDefault = userDB.getDefaultWalletByUUID(uuid) ?: return
        val walletVerification = walletDB.getVerificationWallet(walletDefault)
        if (walletVerification != 1){
            sender.sendMessage("Ваш кошелек не активирован.")
            return
        }
        val walletCurrency = walletDB.getWalletCurrency(walletDefault) ?: return
        val currency = functions.convertStringToMaterial(walletCurrency)
        val typeBlock: Material
        if (currency.second) {
            typeBlock = currency.first!! // Преобразованный материал
        } else return

        val balance = walletDB.getWalletBalance(walletDefault) ?: 0
        if (balance < amount) {
            sender.sendMessage("У вас недостаточно средств на балансе.")
            return
        }

        val item = ItemStack(typeBlock)
        val successful = functions.giveItem(player, item, amount)
        if (successful) {
            walletDB.updateWalletBalance(walletDefault, -amount)
            sender.sendMessage("Вы сняли $amount $currency")
        } else {
            sender.sendMessage("Операция прервана")
        }
    }


}