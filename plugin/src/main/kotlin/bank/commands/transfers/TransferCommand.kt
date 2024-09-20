package bank.commands.transfers

import App.Companion.configPlugin
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class TransferCommand(config: FileConfiguration) : CommandExecutor {
    private val function = Functions()
    private val discordNotifier = DiscordNotifier(config)

    //todo: /transfer [Получатель: Имя пользователя/Id Wallet*] [amount] [comment]

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage("localisation.messages.usage.transfer".localized())
            return true
        }
        val amount = args[1].toIntOrNull()
        if (amount == null || amount <= 0){
            sender.sendMessage("localisation.messages.out.amount-incorrect".localized())
            return true
        }
        var comment = "localisation.empty".localized()
        if (args.size > 2) {
            val potentialComment = args.drop(2).joinToString(" ")
            if (potentialComment.isNotBlank()) {
                comment = potentialComment
            }
        }
        if (comment.length > 128) {
            sender.sendMessage("localisation.messages.out.comment-too-long".localized())
            return true
        }

        val senderPlayer = sender as Player
        val targetName = args[0]
        val uuidSender = senderPlayer.uniqueId.toString()
        val uuidTarget = userDB.getUUIDbyPlayerName(targetName) ?: return true
        val senderWalletID = userDB.getDefaultWalletByUUID(uuidSender) ?: return true
        val targetWalletID = userDB.getDefaultWalletByUUID(uuidTarget) ?: return true

        val walletSenderVerification = walletDB.getVerificationWallet(senderWalletID)
        val walletTargetVerification = walletDB.getVerificationWallet(targetWalletID)
        if (walletSenderVerification != 1){
            sender.sendMessage("localisation.messages.out.wallet.unavailable.sender".localized())
            return true
        }
        if (walletTargetVerification != 1){
            sender.sendMessage("localisation.messages.out.wallet.unavailable.target".localized())
            return true
        }
        if (senderWalletID == targetWalletID){
            sender.sendMessage("localisation.messages.out.wallet-same-thing".localized())
            return true
        }
        if (!walletDB.checkWalletStatus(senderWalletID)){
            sender.sendMessage("localisation.messages.out.wallet.unavailable.sender".localized())
            return true
        }
        if (!walletDB.checkWalletStatus(targetWalletID)) {
            sender.sendMessage("localisation.messages.out.wallet.unavailable.target".localized())
            return true
        }
        val senderBalance = walletDB.getWalletBalance(senderWalletID)
        val targetBalance = walletDB.getWalletBalance(targetWalletID) ?: 0
        val limit = configPlugin.getInt("wallet-limit")
        if (senderBalance == null || senderBalance < amount) {
            sender.sendMessage("localisation.messages.out.wallet.not-balance".localized("balance" to senderBalance.toString()))
            return true
        }
        if (targetBalance + amount > limit){
            val free = (targetBalance + amount - limit).toString()
            sender.sendMessage("localisation.messages.out.wallet.overflow.target".localized("free" to free))
            return true
        }
        val currency1 = walletDB.getWalletCurrency(senderWalletID)
        val currency2 = walletDB.getWalletCurrency(targetWalletID)
        if (currency1 != currency2 || currency1 == null) {
            sender.sendMessage("localisation.messages.out.wallet.currency.mismatch".localized(
                "currencyS" to currency1.toString(),
                "currencyT" to currency2.toString()
            ))
            return true
        }
        val target = function.getPlayerByUUID(uuidTarget)
        if (target != null){
            val operation = walletDB.transferCash(
                sender.name,
                target.name,
                senderWalletID,
                targetWalletID,
                amount,
                currency1,
                1,
                uuidSender,
                uuidTarget,
                comment)
            if (operation){
                // Сообщение отправителю
                sender.sendMessage("localisation.messages.out.wallet.transfer-successfully.sender".localized(
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "target" to target.name,
                    "senderWalletID" to senderWalletID.toString(),
                    "targetWalletID" to targetWalletID.toString(),
                    "comment" to comment
                ))
                // Сообщение получателю
                if (function.isPlayerOnline(uuidTarget)){
                    target.sendMessage("localisation.messages.out.wallet.transfer-successfully.target".localized(
                        "sender" to sender.name,
                        "amount" to amount.toString(),
                        "currency" to currency1.toString(),
                        "senderWalletID" to targetWalletID.toString(),
                        "targetWalletID" to targetWalletID.toString(),
                        "comment" to comment
                    ))
                }
                // Сообщение лог в дискорд
                discordNotifier.sendMessageChannelLog("localisation.discord.logger.transfer-successfully".localized(
                    "walletIDSender" to senderWalletID.toString(),
                    "sender" to sender.name,
                    "walletIDTarget" to targetWalletID.toString(),
                    "target" to target.name,
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "comment" to comment
                ))
            } else {
                sender.sendMessage("localisation.messages.out.wallet.transfer-unsuccessfully".localized())
            }
            return true
        } else {
            val targetName = userDB.getPlayerNameByUUID(uuidTarget) ?: "Unknown"
            val operation = walletDB.transferCash(
                sender.name,
                targetName,
                senderWalletID,
                targetWalletID,
                amount,
                currency1,
                1,
                uuidSender,
                uuidTarget,
                comment)
            if (operation){
                // Сообщение отправителю
                sender.sendMessage("localisation.messages.out.wallet.transfer-successfully.sender".localized(
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "target" to targetName,
                    "senderWalletID" to senderWalletID.toString(),
                    "targetWalletID" to targetWalletID.toString(),
                    "comment" to comment))


                // Сообщение лог в дискорд
                discordNotifier.sendMessageChannelLog("localisation.discord.logger.transfer-successfully".localized(
                    "walletIDSender" to senderWalletID.toString(),
                    "sender" to sender.name,
                    "walletIDTarget" to targetWalletID.toString(),
                    "target" to targetName,
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "comment" to comment))

                // Сообщение получателю в дискорд
                val discordTargetID = userDB.getDiscordIDbyUUID(uuidTarget) ?: return true
                discordNotifier.sendPrivateMessage(discordTargetID, "localisation.messages.out.wallet.transfer-successfully.discord.target".localized(
                    "targetWalletID" to targetWalletID.toString(),
                    "sender" to sender.name,
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "comment" to comment,
                ))


            } else {
                sender.sendMessage("localisation.messages.out.wallet.transfer-unsuccessfully".localized())
            }
            return true
        }
    }
}
