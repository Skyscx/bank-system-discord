package bank.commands.balance

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class WalletBalanceAddCommand(config: FileConfiguration, discordBot: DiscordBot) : CommandExecutor {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(discordBot.getJDA())
    private val channelIdLogger = config.getString("channel-id-logger") ?: "null"
    private val walletLimit = config.getString("wallet-limit") ?: "100000"


    //TODO: Переделать функционал под NickName TESTING

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var senderName = "null"
        if (sender !is Player) {
            sender.sendMessage("Эту команду можно использовать только в игре.")
            senderName = "SERVER"
        } else  {
            senderName = sender.name
        }
        val isBanker = sender.hasPermission("skybank.banker") //TODO: Проверить права TESTING
        if (!isBanker || !sender.isOp) {
            sender.sendMessage("Нет прав!")
            return true
        }
        if (!functions.checkArguments(sender, 2, args, localizationManager.getMessage("localisation.messages.usage.balance-add"))) return true
        val target = args[0]
        val uuid = userDB.getUUIDbyPlayerName(args[0])
        if (uuid == null){
            sender.sendMessage("Не найден игрок")
            return true
        }
        val walletID = userDB.getDefaultWalletByUUID(uuid)
        if (walletID == null) {
            sender.sendMessage("Кошелек не найден.")
            return true
        }
        val amount = args[1].toInt()
        if (amount <= 0 || amount >= walletLimit.toInt()) {
            sender.sendMessage("Неверная сумма.")
            return true
        }
        val successful = walletDB.updateWalletBalance(walletID.toInt(), amount)
        if (successful){
            sender.sendMessage("Вы добавили $amount валюты на кошелек игроку $target")
            if(sender.name != target){
                if (functions.isPlayerOnline(uuid)){
                    val player = functions.getPlayerByUUID(uuid) ?: return true
                    player.sendMessage("Вам выдали $amount на ваш кошелек.")
                } else {
                    val discordID = userDB.getDiscordIDbyUUID(uuid) ?: return true
                    discordNotifier.sendPrivateMessage(discordID, "Ваш аккаунт пополнили на $amount")
                }
            }
            discordNotifier.sendMessageChannel(channelIdLogger, "Игроку $target выдали $amount валюты. \n Выдал - $senderName")
        } else {
            sender.sendMessage("Ошибка выполнения операции.")
        }
        return true
    }
}