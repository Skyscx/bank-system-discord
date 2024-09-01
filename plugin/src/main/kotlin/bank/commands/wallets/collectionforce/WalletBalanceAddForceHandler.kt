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

class WalletBalanceAddForceHandler(config: FileConfiguration, discordBot: DiscordBot){
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(discordBot.getJDA(), config)
    private val walletLimit = config.getString("wallet-limit") ?: "100000"
    //todo: переделать сообщения на конфиг
    fun handleBalanceAddForceCommand(sender: CommandSender, args: Array<String>){
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
        val amount = args[3].toInt()
        if (amount <= 0 || amount >= walletLimit.toInt()) {
            sender.sendMessage("Неверная сумма.")
            return
        }
        val successful = walletDB.updateWalletBalance(walletID.toInt(), amount)
        if (successful){
            sender.sendMessage("Вы добавили $amount валюты на кошелек игроку $target")
            if(sender.name != target){
                if (functions.isPlayerOnline(uuid)){
                    val player = functions.getPlayerByUUID(uuid) ?: return
                    player.sendMessage("Вам выдали $amount на ваш кошелек.")
                } else {
                    val discordID = userDB.getDiscordIDbyUUID(uuid) ?: return
                    discordNotifier.sendPrivateMessage(discordID, "Ваш аккаунт пополнили на $amount")
                }
            }
            discordNotifier.sendMessageChannelLog("Игроку $target выдали $amount валюты. \n Выдал - $senderName")
        } else {
            sender.sendMessage("Ошибка выполнения операции.")
        }
        return
    }
}