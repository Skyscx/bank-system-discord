package bank.commands

import data.Database
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class NewTransferCommand(private val database: Database) : CommandExecutor {
    private val function = Functions()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Используйте: /transfer <WalletID/WalletName (S)> <WalletID/WalletName (T)> <amount>")
            return false
        }

        when (args[0].lowercase(Locale.getDefault())) {
            "default" -> handleTransferDefault(sender, args) //Переводы между основными кошельками
            else -> handleTransfer(sender, args) //Переводы по идентификаторам
        }
        return true
    }

    private fun handleTransfer(sender: CommandSender, args: Array<out String>) {
        if (args.size != 3) {
            sender.sendMessage("Используйте: /transfer <WalletID/WalletName (S)> <WalletID/WalletName (T)> <amount>")
            return
        }

        val sourceWallet = args[0]
        val targetWallet = args[1]
        val amount = args[2].toIntOrNull()

        if (amount == null || amount <= 0) {
            sender.sendMessage("Неверная сумма.")
            return
        }

        val sourceWalletID = database.getWalletID(sourceWallet)
        val targetWalletID = database.getWalletID(targetWallet)

        if (sourceWalletID == null) {
            sender.sendMessage("Ваш кошелек не найден.")
            return
        }

        if (targetWalletID == null) {
            sender.sendMessage("Кошелек получателя не найден.")
            return
        }

        if (sourceWalletID == targetWalletID) {
            sender.sendMessage("Транзакция не может быть произведена на одинаковых кошельках.")
            return
        }

        val player = sender as Player
        val uuidWalletSource = database.getUUIDbyWalletID(sourceWalletID)

        if (uuidWalletSource != player.uniqueId.toString()) {
            sender.sendMessage("Вы не владелец этого кошелька!")
            return
        }

        if (database.isWalletStatusZero(sourceWalletID)) {
            sender.sendMessage("Ваш кошелек недоступен.")
            return
        }

        if (database.isWalletStatusZero(targetWalletID)) {
            sender.sendMessage("Кошелек получателя недоступен.")
            return
        }

        // Проверка баланса кошельков
        val sourceBalance = database.getWalletBalance(sourceWalletID)
        if (sourceBalance < amount) {
            sender.sendMessage("Недостаточно средств на счете отправителя.")
            return
        }

        // Проверка валют
        val currency1 = database.getWalletCurrency(sourceWalletID)
        val currency2 = database.getWalletCurrency(targetWalletID)

        if (currency1 != currency2 || currency1 == null) {
            sender.sendMessage("Валюты кошельков не совпадают.")
            return
        }

        // Получение данных для отправки запроса
        val targetUUID = database.getUUIDbyWalletID(targetWalletID)
        val target = function.getPlayerByUUID(targetUUID.toString())

        if (target == null) {
            sender.sendMessage("Получатель не найден.")
            return
        }

        val operation = database.transferCash(player, target, sourceWalletID, targetWalletID, amount, currency1, 1)

        if (operation) {
            sender.sendMessage("Вы перевели $amount $currency1 игроку $target (#$sourceWalletID -> #$targetWalletID)")
        } else {
            sender.sendMessage("Ошибка при выполнении перевода.")
        }
    }

    private fun handleTransferDefault(sender: CommandSender, args: Array<out String>) {
        if (args.size != 3) {
            sender.sendMessage("Используйте: /transfer default <Player (T)> <amount>")
            return
        }

        val targetName = args[1]
        val amount = args[2].toIntOrNull()
        val senderPlayer = sender as Player

        if (amount == null || amount <= 0) {
            sender.sendMessage("Неверная сумма.")
            return
        }

        val uuidTarget = database.getUUIDbyPlayerName(targetName)

        if (uuidTarget == null) {
            sender.sendMessage("Получатель не найден.")
            return
        }

        val target = function.getPlayerByUUID(uuidTarget)

        if (target == null) {
            sender.sendMessage("Получатель не найден.")
            return
        }

        val sourceWalletID = database.getDefaultWalletIDByUUID(senderPlayer.uniqueId.toString())
        val targetWalletID = database.getDefaultWalletIDByUUID(target.uniqueId.toString())

        if (sourceWalletID == null) {
            sender.sendMessage("Ваш кошелек не найден.")
            return
        }

        if (targetWalletID == null) {
            sender.sendMessage("Кошелек получателя не найден.")
            return
        }

        if (database.isWalletStatusZero(sourceWalletID)) {
            sender.sendMessage("Ваш кошелек недоступен.")
            return
        }

        if (database.isWalletStatusZero(targetWalletID)) {
            sender.sendMessage("Кошелек получателя недоступен.")
            return
        }

        if (sourceWalletID == targetWalletID) {
            sender.sendMessage("Транзакция не может быть произведена на одинаковых кошельках.")
            return
        }

        // Проверка баланса кошельков
        val sourceBalance = database.getWalletBalance(sourceWalletID)
        if (sourceBalance < amount) {
            sender.sendMessage("Недостаточно средств на счете отправителя.")
            return
        }

        // Проверка валют
        val currency1 = database.getWalletCurrency(sourceWalletID)
        val currency2 = database.getWalletCurrency(targetWalletID)

        if (currency1 != currency2 || currency1 == null) {
            sender.sendMessage("Валюты кошельков не совпадают.")
            return
        }

        val operation = database.transferCash(senderPlayer, target, sourceWalletID, targetWalletID, amount, currency1, 1)

        if (operation) {
            sender.sendMessage("Вы перевели $amount $currency1 игроку $target (#$sourceWalletID -> #$targetWalletID)")
        } else {
            sender.sendMessage("Ошибка при выполнении перевода.")
        }
    }

}