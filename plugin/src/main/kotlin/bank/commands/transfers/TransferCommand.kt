package bank.commands.transfers

import App.Companion.configPlugin
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

class TransferCommand(config: FileConfiguration, discordBot: DiscordBot) : CommandExecutor {
    private val function = Functions()
    private val discordNotifier = DiscordNotifier(config)

    //todo: /transfer [Получатель: Имя пользователя/Id Wallet*] [amount] [comment]

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.usage.transfer"))
            return true
        }
        val amount = args[1].toIntOrNull()
        if (amount == null || amount <= 0){
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.amount-incorrect"))
            return true
        }
        var comment = "Отсутствует"
        if (args.size > 2) {
            val potentialComment = args.drop(2).joinToString(" ")
            if (potentialComment.isNotBlank()) {
                comment = potentialComment
            }
        }
        if (comment.length > 128) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.comment-too-long"))
            return true
        }


        val senderPlayer = sender as Player
        val targetName = args[0]
        val uuidSender = senderPlayer.uniqueId.toString()
        val uuidTarget = userDB.getUUIDbyPlayerName(targetName)
        if (uuidTarget == null) {
            // Сообщение о том что пользователь не зарегистрирован в банке.
            sender.sendMessage("todo")
            return true
        }
        val senderWalletID = userDB.getDefaultWalletByUUID(uuidSender).toString()
        val targetWalletID = userDB.getDefaultWalletByUUID(uuidTarget).toString()
        if (senderWalletID == "null"){
            sender.sendMessage("todo: у вас нет кошелька")
            return true
        }
        if (targetWalletID == "null"){
            sender.sendMessage("todo: У получателя нет доступного кошелька")
            return true
        }


        val walletSenderVerification = walletDB.getVerificationWallet(senderWalletID.toInt())
        val walletTargetVerification = walletDB.getVerificationWallet(targetWalletID.toInt())
        if (walletSenderVerification != 1){
            sender.sendMessage("Ваш кошелек не активирован.")
            return true
        }
        if (walletTargetVerification != 1){
            sender.sendMessage("Кошелек получателя не активирован.")
            return true
        }
        if (senderWalletID == targetWalletID){
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-same-thing"))
            return true
        }
        if (!walletDB.checkWalletStatus(senderWalletID.toInt())){
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.unavailable.sender"))
            return true
        }
        if (!walletDB.checkWalletStatus(targetWalletID.toInt())) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.unavailable.target"))
            return true
        }
        val senderBalance = walletDB.getWalletBalance(senderWalletID.toInt())
        val targetBalance = walletDB.getWalletBalance(targetWalletID.toInt()) ?: 0
        val limit = configPlugin.getInt("wallet-limit")
        if (senderBalance == null || senderBalance < amount) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.not-balance", "balance" to senderBalance.toString()))
            return true
        }
        if (targetBalance + amount > limit){
            val free = (targetBalance + amount - limit).toString()
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.overflow.target", "free" to free))
            return true
        }
        val currency1 = walletDB.getWalletCurrency(senderWalletID.toInt())
        val currency2 = walletDB.getWalletCurrency(targetWalletID.toInt())
        if (currency1 != currency2 || currency1 == null) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.currency.mismatch",
                "currencyS" to currency1.toString(),
                "currencyT" to currency2.toString()))
            return true
        }
        val target = function.getPlayerByUUID(uuidTarget)
        if (target != null){
            //Сообщение в игру
            val operation = walletDB.transferCash(sender.name, target.name, senderWalletID.toInt(), targetWalletID.toInt(), amount, currency1, 1, uuidSender, uuidTarget, comment)
            if (operation){
                // Сообщение отправителю
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.sender",
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "target" to target.name,
                    "senderWalletID" to senderWalletID,
                    "targetWalletID" to targetWalletID,
                    "comment" to comment))

                // Сообщение получателю
                if (function.isPlayerOnline(uuidTarget)){
                    target.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.target",
                        "sender" to sender.name,
                        "amount" to amount.toString(),
                        "currency" to currency1.toString(),
                        "senderWalletID" to targetWalletID,
                        "targetWalletID" to targetWalletID,
                        "comment" to comment))
                }

                // Сообщение лог в дискорд
                discordNotifier.sendMessageChannelLog(localizationManager.getMessage("localisation.discord.logger.transfer-successfully",
                    "walletIDSender" to senderWalletID,
                    "sender" to sender.name,
                    "walletIDTarget" to targetWalletID,
                    "target" to target.name,
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "comment" to comment))
            } else {
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-unsuccessfully"))
            }
            return true
        } else {
            val targetName = userDB.getPlayerNameByUUID(uuidTarget) ?: "Unknown"
            val operation = walletDB.transferCash(sender.name, targetName, senderWalletID.toInt(), targetWalletID.toInt(), amount, currency1, 1, uuidSender, uuidTarget, comment)
            if (operation){
                // Сообщение отправителю
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.sender",
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "target" to targetName,
                    "senderWalletID" to senderWalletID,
                    "targetWalletID" to targetWalletID,
                    "comment" to comment))

                // Сообщение получателю в дискорд

                val discordTargetID = userDB.getDiscordIDbyUUID(uuidTarget) ?: return true
                discordNotifier.sendPrivateMessage(discordTargetID, localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.discord.target",
                    "targetWalletID" to targetWalletID,
                    "sender" to sender.name,
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "comment" to comment,
                ))

                // Сообщение лог в дискорд
                discordNotifier.sendMessageChannelLog(localizationManager.getMessage("localisation.discord.logger.transfer-successfully",
                    "walletIDSender" to senderWalletID,
                    "sender" to sender.name,
                    "walletIDTarget" to targetWalletID,
                    "target" to targetName,
                    "amount" to amount.toString(),
                    "currency" to currency1,
                    "comment" to comment))
            } else {
                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-unsuccessfully"))
            }
            return true
        }
    }
}
