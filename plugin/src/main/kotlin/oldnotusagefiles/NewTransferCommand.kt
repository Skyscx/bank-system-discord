//package bank.commands.transfers
//
//import App.Companion.configPlugin
//import App.Companion.localizationManager
//import App.Companion.userDB
//import App.Companion.walletDB
//import discord.dsbot.DiscordBot
//import discord.dsbot.DiscordNotifier
//import functions.Functions
//import org.bukkit.command.Command
//import org.bukkit.command.CommandExecutor
//import org.bukkit.command.CommandSender
//import org.bukkit.configuration.file.FileConfiguration
//import org.bukkit.entity.Player
//import java.util.*
//
//class NewTransferCommand(config: FileConfiguration, discordBot: DiscordBot) : CommandExecutor {
//    private val function = Functions()
//    private val discordNotifier = DiscordNotifier(discordBot.getJDA())
//    private val channelIdLogger = config.getString("channel-id-logger") ?: "null"
//
//
//    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        if (args.isEmpty()) {
//            sender.sendMessage("Используйте: /transfer <WalletID/WalletName (S)> <WalletID/WalletName (T)> <amount>")
//            return false
//        }
//
//        when (args[0].lowercase(Locale.getDefault())) {
//            "default" -> handleTransfer(sender, args, true)
//            else -> handleTransfer(sender, args, false)
//        }
//        return true
//    }
//
//    private fun handleTransfer(sender: CommandSender, args: Array<out String>, isDefault: Boolean) {
//        if (args.size != 3) {
//            sender.sendMessage(localizationManager.getMessage(if (isDefault) "localisation.messages.usage.transfer.default" else "localisation.messages.usage.transfer"))
//            return
//        }
//
//        val amount = args[2].toIntOrNull()
//        if (amount == null || amount <= 0) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.amount-incorrect"))
//            return
//        }
//
//        val (sourceWalletID, targetWalletID) = if (isDefault) {
//            val senderPlayer = sender as Player
//            val targetName = args[1]
//            val uuidTarget = userDB.getUUIDbyPlayerName(targetName) ?: run {
//                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.target-not-found"))
//                return
//            }
//            val target = function.getPlayerByUUID(uuidTarget) ?: run {
//                sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.target-not-found"))
//                return
//            }
//            walletDB.getDefaultWalletIDByUUID(senderPlayer.uniqueId.toString()) to walletDB.getDefaultWalletIDByUUID(target.uniqueId.toString())
//        }
//        else {
//            val sourceWallet = args[0]
//            val targetWallet = args[1]
//            walletDB.getWalletID(sourceWallet) to walletDB.getWalletID(targetWallet)
//        }
//
//        if (sourceWalletID == null) {
//            val walletowner = localizationManager.getMessage("localisation.sender")
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null", "wallet-owner" to walletowner))
//            return
//        }
//
//        if (targetWalletID == null) {
//            val walletowner = localizationManager.getMessage("localisation.target")
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null", "wallet-owner" to walletowner))
//            return
//        }
//
//        if (sourceWalletID == targetWalletID) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-same-thing"))
//            return
//        }
//
//        val player = sender as Player
//        val uuidWalletSource = walletDB.getUUIDbyWalletID(sourceWalletID)
//
//        if (uuidWalletSource != player.uniqueId.toString()) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.not-owner"))
//            return
//        }
//
//        if (!walletDB.checkWalletStatus(sourceWalletID)) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.unavailable.sender"))
//            return
//        }
//        if (!walletDB.checkWalletStatus(targetWalletID)) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.unavailable.target"))
//            return
//        }
//
//        val senderBalance = walletDB.getWalletBalance(sourceWalletID)
//        val targetBalance = walletDB.getWalletBalance(targetWalletID) ?: 0
//        val limit = configPlugin.getInt("wallet-limit")
//        if (senderBalance == null || senderBalance < amount) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.not-balance"))
//            return
//        }
//        if (targetBalance + amount > limit){
//            val free = (targetBalance + amount - limit).toString()
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.overflow.target", "free" to free))
//            return
//        }
//
//        val currency1 = walletDB.getWalletCurrency(sourceWalletID)
//        val currency2 = walletDB.getWalletCurrency(targetWalletID)
//
//        if (currency1 != currency2 || currency1 == null) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.currency.mismatch",
//                "currencyS" to currency1.toString(), "currencyT" to currency2.toString()))
//            return
//        }
//
//        val targetUUID = walletDB.getUUIDbyWalletID(targetWalletID)
//        val target = function.getPlayerByUUID(targetUUID.toString())
//
//        if (target == null) {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.target-not-found"))
//            return
//        }
//
//        val operation = walletDB.transferCash(player, target.name, sourceWalletID, targetWalletID, amount, currency1, 1, "s", "t")
//
//        if (operation) {
//            // Сообщение отправителю
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.sender",
//                "amount" to amount.toString(),
//                "currency" to currency1,
//                "target" to target.name,
//                "senderWalletID" to sourceWalletID.toString(),
//                "targetWalletID" to targetWalletID.toString()))
//            // Сообщение получателю //TODO: Testing
//            if (function.isPlayerOnline(targetUUID.toString())){
//                target.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.target",
//                    "sender" to sender.name,
//                    "amount" to amount.toString(),
//                    "currency" to currency1.toString(),
//                    "senderWalletID" to sourceWalletID.toString(),
//                    "targetWalletID" to targetWalletID.toString()))
//            } else {
//                // Сообщение в личные сообщения Discord User Target //TODO: Реализовать, обязательно отправлять если игрок не в сети. TESTING
//                val discordTargetID = userDB.getDiscordIDbyUUID(targetUUID.toString()) ?: "608291538207899689"
//                discordNotifier.sendPrivateMessage(discordTargetID, localizationManager.getMessage("localisation.messages.out.wallet.transfer-successfully.discord.target",
//                    "targetWalletID" to targetWalletID.toString(),
//                    "sender" to sender.name,
//                    "amount" to amount.toString(),
//                    "currency" to currency1
//                    ))
//            }
//            // Сообщение в личные сообщения Discord User Sender //TODO: Реализовать в доп.обновах (НЕ ВАЖНОЕ ОБНОВЛЕНИЕ)
//            // Сообщение в личные сообщения Discord User Target //TODO: Реализовать в доп.обновах (НЕ ВАЖНОЕ ОБНОВЛЕНИЕ)
//
//
//            // Сообщение лог в дискорд
//            discordNotifier.sendMessageChannel(channelIdLogger, localizationManager.getMessage("localisation.discord.logger.transfer-successfully",
//                "walletIDSender" to sourceWalletID.toString(),
//                "sender" to sender.name,
//                "walletIDTarget" to targetWalletID.toString(),
//                "target" to target.name,
//                "amount" to amount.toString(),
//                "currency" to currency1))
//        } else {
//            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.transfer-unsuccessfully"))
//        }
//    }
//}