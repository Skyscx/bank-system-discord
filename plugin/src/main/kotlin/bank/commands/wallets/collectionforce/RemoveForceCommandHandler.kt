package bank.commands.wallets.collectionforce

import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

class RemoveForceCommandHandler(config: FileConfiguration) {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    fun handleRemoveForceCommand(sender: CommandSender, args: Array<String>){
        if (!functions.checkArguments(sender, 2, args, "localisation.messages.usage.account.force.remove".localized())) return
        val target = args[1]
        val targetUUID = userDB.getUUIDbyPlayerName(target)
        if (targetUUID == null){
            sender.sendMessage("localisation.error.not-search-target".localized())
            return
        }
        val defaultWalletID = userDB.getDefaultWalletByUUID(targetUUID) ?: 0
        val successful = walletDB.deleteUserWallet(defaultWalletID)
        if (successful){
            sender.sendMessage("localisation.messages.out.banker.wallet.remove.successful".localized( "target" to target))
            val targetPlayer = functions.getPlayerByUUID(targetUUID)
            if (targetPlayer == null){
                val discordIDTarget = userDB.getDiscordIDbyUUID(targetUUID) ?: return
                discordNotifier.sendPrivateMessage(discordIDTarget, "localisation.discord.private.wallet.remove-successfully.forced".localized())
            } else {
                targetPlayer.sendMessage("localisation.messages.out.wallet.remove-successfully.forced".localized())
            }
            discordNotifier.sendMessageChannelLog("localisation.discord.logger.remove-successfully.forced".localized("senderName" to sender.name, "target" to target))
            return
        }
    }
}