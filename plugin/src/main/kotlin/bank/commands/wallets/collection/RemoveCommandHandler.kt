package bank.commands.wallets.collection

import App.Companion.historyDB
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class RemoveCommandHandler(config: FileConfiguration) {
    val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)

    fun handleRemoveCommand(player: Player, args: Array<String>) {


        val playerUUID = player.uniqueId
        userDB.isPlayerExists(playerUUID).thenAccept { exists ->
            if (exists) {
                if (!functions.checkArguments(player, 3, args, "localisation.messages.usage.account.remove.boolean".localized())) return@thenAccept
                val bool = args[2].toBooleanStrictOrNull() ?: false
                if (!bool) {
                    player.sendMessage("localisation.messages.usage.account.remove.boolean".localized())
                    return@thenAccept
                }
                val walletID = userDB.getDefaultWalletByUUID(playerUUID.toString()) ?: return@thenAccept
                val balance = walletDB.getWalletBalance(walletID).toString()
                val currency = walletDB.getWalletCurrency(walletID).toString()
                val successful = walletDB.deleteUserWallet(walletID)
                if (successful) {
                    player.sendMessage("localisation.messages.out.wallet.remove-successfully.sender".localized())
                    historyDB.insertBankHistory(
                        typeOperation = "CLOSE_WALLET",
                        senderName = player.name,
                        senderWalletID = walletID,
                        uuidSender = playerUUID.toString(),
                        amount = 15, // todo: Сделать из ДБ
                        currency = "DIAMOND_ORE", //todo: Сделать из дб
                        status = 1,

                        uuidTarget = "null",
                        comment = "null",
                        targetWalletID = 0,
                        targetName = "null"
                    )
                    discordNotifier.sendMessageChannelLog("localisation.discord.logger.remove-successfully".localized(
                        "player" to player.name,
                        "amount" to balance,
                        "currency" to currency
                    ))
                } else {
                    player.sendMessage("localisation.messages.out.wallet.remove-unsuccessfully.sender".localized())
                }
            } else {
                player.sendMessage("localisation.error.not-search-target".localized())
            }
        }.exceptionally { e ->
            e.printStackTrace()
            null
        }
    }
}