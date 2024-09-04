package bank.commands.wallets.collectionforce

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

class RemoveForceCommandHandler(config: FileConfiguration, discordBot: DiscordBot) {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    fun handleRemoveForceCommand(sender: CommandSender, args: Array<String>){
        if (!functions.checkArguments(sender, 2, args, localizationManager.getMessage("localisation.messages.usage.account.force.remove"))) return

        val target = args[1]
        val targetUUID = userDB.getUUIDbyPlayerName(target)
        if (targetUUID == null){
            sender.sendMessage("не найден игрок")
            return
        }
        val defaultWalletID = userDB.getDefaultWalletByUUID(targetUUID) ?: 0
        val successful = walletDB.deleteUserWallet(defaultWalletID)
        if (successful){
            sender.sendMessage("Кошелек игрока $target был удален.")
            val targetPlayer = functions.getPlayerByUUID(targetUUID)
            if (targetPlayer == null){
                val discordIDTarget = userDB.getDiscordIDbyUUID(targetUUID) ?: return
                discordNotifier.sendPrivateMessage(discordIDTarget, "вам удалили кошелек")
            } else {
                targetPlayer.sendMessage("Вам удалили кошелек.")
            }
            discordNotifier.sendMessageChannelLog("${sender.name} удалил кошелек игрока $target")
            return
        }





    }
}