package bank.commands.balance

import App.Companion.walletDB
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WalletBalanceAddCommand() : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            //tODO: по возможности сделать консольной командой
            return true
        }
        if (args.isEmpty()) return false
        val isBanker = sender.hasPermission("skybank.banker") //TODO: Проверить права
        if (!isBanker || !sender.isOp) {
            sender.sendMessage("Нет прав!")
            return true
        }
        if (args.size != 2) return false
        val walletID= walletDB.getWalletID(args[0])
        if (walletID == null) {
            sender.sendMessage("Кошелек не найден.") //todo: сделать +по имени
            return true
        }
        val amount = args[1].toInt()
        if (amount <= 0) {
            sender.sendMessage("Неверная сумма.")
            return true
        }
        val isSet = walletDB.updateWalletBalance(walletID, amount)
        if (isSet){
            sender.sendMessage("Вы добавили $amount валюты на кошелек #$walletID")
            //TODO: Сообщение для получателя в игре
            //TODO: Сообщение для получателя в боте
            //TODO: Сообщение для логирования.
            //TODO: Сделать логирование(Хотя бы в консоль (не через println))
        } else {
            sender.sendMessage("Ошибка выполнения операции.")
        }
        return true
    }
}