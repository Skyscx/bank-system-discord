package bank.commands.wallets.collectionforce

import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class WalletBalanceAddForceHandler(config: FileConfiguration){
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    private val walletLimit = config.getString("wallet-limit") ?: "100000"
    //todo: переделать сообщения на конфиг
    fun handleBalanceAddForceCommand(sender: CommandSender, args: Array<String>){
        val senderName = if (sender !is Player) { "SERVER" } else  { sender.name }
        if (!functions.hasPermission(sender, "skybank.banker")) {
            sender.sendMessage("localisation.messages.out.no-permissions".localized())
            return
        }
        if (!functions.checkArguments(sender, 4, args, "localisation.messages.usage.balance-add".localized())) return
        val target = args[2]
        val uuid = userDB.getUUIDbyPlayerName(target) ?: return
        val walletID = userDB.getDefaultWalletByUUID(uuid) ?: return
        val amount = args[3].toInt()
        if (amount <= 0 || amount >= walletLimit.toInt()) {
            sender.sendMessage("localisation.messages.out.amount-incorrect".localized())
            return
        }
        val successful = walletDB.updateWalletBalance(walletID, amount)
        if (successful){
            sender.sendMessage("localisation.messages.out.banker.wallet.balance-add-successfully.force".localized( "amount" to amount.toString(), "target" to target))
            if(sender.name != target){
                if (functions.isPlayerOnline(uuid)){
                    val player = functions.getPlayerByUUID(uuid) ?: return
                    player.sendMessage("localisation.messages.out.wallet.add-balance.forced".localized(
                        "amount" to amount.toString()))
                } else {
                    val discordID = userDB.getDiscordIDbyUUID(uuid) ?: return
                    discordNotifier.sendPrivateMessage(discordID, "localisation.discord.private.wallet.add-balance.forced".localized( "amount" to amount.toString()))
                }
            }
            discordNotifier.sendMessageChannelLog("localisation.discord.logger.add-balance-successfully.forced".localized(
                "target" to target,
                "amount" to amount.toString(),
                "senderName" to senderName))
        } else {
            sender.sendMessage("localisation.messages.out.banker.wallet.balance-add-unsuccessfully.force".localized())
        }
        return
    }
}