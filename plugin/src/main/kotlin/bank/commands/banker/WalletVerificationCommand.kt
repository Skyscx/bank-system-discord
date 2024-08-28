package bank.commands.banker

import App.Companion.localizationManager
import App.Companion.walletDB
import discord.FunctionsDiscord
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WalletVerificationCommand : CommandExecutor {
    private val functionDiscord = FunctionsDiscord()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.player-command"))
            return true
        }
        if (args.isEmpty()) return false
        when(args[0].lowercase()){
            "list" -> {
                val list = walletDB.getUnverifiedWallets()
                for (id in list){
                    val playerData = walletDB.getPlayerDataByID(id.toInt())
                    sender.sendMessage(playerData ?: localizationManager.getMessage("localisation.messages.out.banker.list-verify-empty"))
                }
            }
            else -> {
                if (args.size != 2) return false
                val id = args[0].toIntOrNull() ?: return false
                val bool = args[1].toBooleanStrictOrNull() ?: return false
                if (!walletDB.doesIdExistWallet(id)){
                    sender.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet-null", "wallet-owner" to id.toString()))
                    return true
                }
                val verification = walletDB.getVerificationWallet(id)
                val player = walletDB.getPlayerByWalletID(id)?.name.toString()
                val inspector = functionDiscord.getPlayerDiscordID(sender.uniqueId).toString()
                when(verification){
                    0 ->{
                        if (bool){
                            walletDB.setVerificationWallet(id,1)
                            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.banker.wallet.open", "player" to player))
                        } else {
                            walletDB.setVerificationWallet(id,-1)
                            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.banker.wallet.reject", "player" to player))
                        }
                        walletDB.setInspectorWallet(id, inspector)
                        walletDB.setVerificationWalletDate(id)
                    }
                    1 -> sender.sendMessage(localizationManager.getMessage("localisation.messages.out.banker.wallet.already.open"))
                    -1 -> sender.sendMessage(localizationManager.getMessage("localisation.messages.out.banker.wallet.already.reject"))
                    else -> sender.sendMessage(localizationManager.getMessage("localisation.messages.out.error-search-data-in-database"))
                }
            }
        }
        return true
    }
}