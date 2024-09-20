package bank.commands.wallets.collectionforce

import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class WalletBalanceRemoveForceHandler(config: FileConfiguration){
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    fun handleBalanceRemoveForceCommand(sender: CommandSender, args: Array<String>){
        val senderName = if (sender !is Player) { "SERVER" } else  { sender.name }
        if (!functions.hasPermission(sender, "skybank.banker")) {
            sender.sendMessage("localisation.messages.out.no-permissions".localized())
            return
        }
        if (!functions.checkArguments(sender, 4, args, "localisation.messages.usage.balance-add".localized())) return
        val target = args[2]
        val uuid = userDB.getUUIDbyPlayerName(target) ?: return
        val walletID = userDB.getDefaultWalletByUUID(uuid) ?: return
        var amount = args[3].toInt()
        val balance = walletDB.getWalletBalance(walletID) ?: 0
        if (amount <= 0) {
            sender.sendMessage("localisation.messages.out.amount-incorrect".localized())
            return
        }
        if (amount >= balance) amount = balance
        val successful = walletDB.updateWalletBalance(walletID, -amount)
        if (successful){
            sender.sendMessage("localisation.messages.out.banker.wallet.balance-get-successfully.force".localized("amount" to amount.toString(), "target" to target))
            if(sender.name != target){
                if (functions.isPlayerOnline(uuid)){
                    val player = functions.getPlayerByUUID(uuid) ?: return
                    player.sendMessage("localisation.messages.out.wallet.get-balance.forced".localized( "amount" to amount.toString()))
                } else {
                    val discordID = userDB.getDiscordIDbyUUID(uuid) ?: return
                    discordNotifier.sendPrivateMessage(discordID, "localisation.discord.private.wallet.get-balance.forced".localized( "amount" to amount.toString()))
                }
            }
            discordNotifier.sendMessageChannelLog("localisation.discord.logger.get-balance-successfully.forced".localized( "target" to target, "amount" to amount.toString(), "senderName" to senderName))
        } else {
            sender.sendMessage("localisation.messages.out.banker.wallet.balance-get-unsuccessfully.force".localized())
        }
        return
    }
}