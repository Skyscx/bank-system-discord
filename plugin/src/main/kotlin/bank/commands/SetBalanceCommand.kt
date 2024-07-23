package bank.commands

import database.Database
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetBalanceCommand(private val database: Database) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            return true
        }

        if (args.size != 2) {
            sender.sendMessage("Использование: /set-balance <игрок> <сумма>")
            return true
        }

        val targetPlayerName = args[0]
        val targetPlayer = Bukkit.getPlayer(targetPlayerName)
        if (targetPlayer == null) {
            sender.sendMessage("Игрок $targetPlayerName не найден.")
            return true
        }

        val amount = args[1].toInt()
        if (amount <= 0) {
            sender.sendMessage("Неверная сумма.")
            return true
        }

//        val senderBalance = database.getPlayerBalance(sender.uniqueId)
//        if (senderBalance < amount) {
//            sender.sendMessage("У вас недостаточно средств.")
//            return true
//        }
        val uuidTarger = targetPlayer.uniqueId
        database.setPlayerBalance(uuidTarger, amount)

        sender.sendMessage("Вы установили $amount монет игроку $targetPlayerName.")
        targetPlayer.sendMessage("Игрок ${sender.name} установил вам $amount монет.")

        return true
    }
}