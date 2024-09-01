package bank.commands.wallets.collectionforce

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class WalletBalanceRemoveForceHandler(config: FileConfiguration, discordBot: DiscordBot){
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(discordBot.getJDA(), config)
    fun handleBalanceRemoveForceCommand(sender: CommandSender, args: Array<String>){
        val senderName = if (sender !is Player) { "SERVER" } else  { sender.name }
        if (!functions.hasPermission(sender, "skybank.banker")) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.no-permissions"))
            return
        }
        if (!functions.checkArguments(sender, 4, args, localizationManager.getMessage("localisation.messages.usage.balance-add"))) return
        val target = args[2]
        val uuid = userDB.getUUIDbyPlayerName(target)
        if (uuid == null){
            sender.sendMessage("Не найден игрок")
            return
        }
        val walletID = userDB.getDefaultWalletByUUID(uuid)
        if (walletID == null) {
            sender.sendMessage("Кошелек не найден.")
            return
        }
        var amount = args[3].toInt()
        val balance = walletDB.getWalletBalance(walletID) ?: 0
        if (amount <= 0) {
            sender.sendMessage("Неверная сумма")
            return
        }
        if (amount >= balance) amount = balance
        val successful = walletDB.updateWalletBalance(walletID.toInt(), -amount)
        if (successful){
            sender.sendMessage("Вы изъяли $amount валюты с кошелька игрока $target")
            if(sender.name != target){
                if (functions.isPlayerOnline(uuid)){
                    val player = functions.getPlayerByUUID(uuid) ?: return
                    player.sendMessage("У вас изъяли $amount с вашего кошелька.")
                } else {
                    val discordID = userDB.getDiscordIDbyUUID(uuid) ?: return
                    discordNotifier.sendPrivateMessage(discordID, "С вашего кошелька изъяли $amount валюты")
                }
            }
            discordNotifier.sendMessageChannelLog("Игроку $target изъяли $amount валюты. \n Украл - $senderName")
        } else {
            sender.sendMessage("Ошибка выполнения операции.")
        }
        return
    }
}