package bank.commands.wallets.collectionforce

import App.Companion.historyDB
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import java.util.*

class RenamePlayerNameUser(config: FileConfiguration) {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    //todo: /wallet-force rename [uuid] [newNamePlayer] CONSOLE COMMAND
    fun handleRenamePlayerNameUser(sender: CommandSender, args: Array<String>){
        if (!functions.checkArguments(sender, 3, args, "localisation.messages.usage.account.force.rename".localized())) return
        val targetUUID = args[1]
        try {
            val targetUUID = UUID.fromString(targetUUID)
            userDB.isPlayerExists(targetUUID).thenAccept{ exists ->
                if (exists){
                    val newPlayerName = args[2]
                    userDB.setPlayerNameByUUID(targetUUID.toString(), newPlayerName)
                    if (functions.senderIsPlayer(sender).second){
                        sender.sendMessage("localisation.messages.out.banker.wallet.rename-player".localized(
                            "targetUUID" to targetUUID.toString(), "newPlayerName" to newPlayerName
                        ))
                    }
                    val walletID = walletDB.getDefaultWalletIDByUUID(targetUUID.toString()) ?: return@thenAccept
                    val balance = walletDB.getWalletBalance(walletID) ?: return@thenAccept

                    historyDB.insertBankHistory( //todo: Сделать норм лог
                        typeOperation = "RENAMING_PLAYER",
                        senderName = "BANKER",
                        senderWalletID = 0,
                        uuidSender = "null",
                        amount = 0,
                        currency = "null",
                        status = 1,
                        oldBalance = balance,
                        newBalance = balance,
                        uuidTarget = targetUUID.toString(),
                        comment = "localisation.messages.out.history.comment.rename-player".localized(),
                        targetWalletID = walletID,
                        targetName = newPlayerName
                    )
                    discordNotifier.sendMessageChannelLog("localisation.discord.logger.rename-successfully.forced".localized(
                        "sender" to sender.name, "newPlayerName" to newPlayerName, "targetUUID" to targetUUID.toString()
                    ))
                    return@thenAccept
                }
                sender.sendMessage("localisation.error.not-search-target".localized())
            }.exceptionally { e ->
                e.printStackTrace()
                null
            }
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("localisation.error.invalid.uuid".localized("uuid" to targetUUID))
            return
        }
    }
}