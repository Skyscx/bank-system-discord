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
        val player = sender as Player
        val uuid = userDB.getUUIDbyPlayerName(player.name)
        if (uuid == null) {
            sender.sendMessage("нет в бд такого игрока")
            return
        }
        val walletDefault = userDB.getDefaultWalletByUUID(uuid)
        if (walletDefault == null) {
            sender.sendMessage("Ошибка иницилизации кошелькап")
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
            sender.sendMessage("Ошибка иницилизации валюты")
            return

        }
        val item = ItemStack(typeBlock)
        val successful = functions.giveItem(player, item, amount)
        if (successful){
            walletDB.updateWalletBalance(walletDefault, -amount)
            sender.sendMessage("Вы сняли $amount $currency")
        } else {
            sender.sendMessage("Операция прервана")
        }
        return
    }
}