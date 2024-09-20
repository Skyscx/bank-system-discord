package bank.commands.banker

import App.Companion.localized
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
            sender.sendMessage("localisation.messages.out.player-command".localized())
            return true
        }
        if (args.isEmpty()) return false
        when(args[0].lowercase()){
            "list" -> {
                val list = walletDB.getUnverifiedWallets()
                for (id in list){
                    val playerData = walletDB.getPlayerDataByID(id.toInt())
                    sender.sendMessage(playerData ?: "localisation.messages.out.banker.list-verify-empty".localized())
                }
            }
            else -> {
                if (args.size != 2) return false
                val id = args[0].toIntOrNull() ?: return false
                val bool = args[1].toBooleanStrictOrNull() ?: return false
                if (!walletDB.doesIdExistWallet(id)){
                    sender.sendMessage("localisation.messages.out.wallet-null".localized("wallet-owner" to id.toString()))
                    return true
                }
                val verification = walletDB.getVerificationWallet(id)
                val player = walletDB.getPlayerByWalletID(id)?.name.toString()
                val inspector = functionDiscord.getPlayerDiscordID(sender.uniqueId).toString()
                when(verification){
                    0 ->{
                        if (bool){
                            walletDB.setVerificationWallet(id,1)
                            sender.sendMessage("localisation.messages.out.banker.wallet.open".localized("player" to player))
                        } else {
                            walletDB.setVerificationWallet(id,-1)
                            sender.sendMessage("localisation.messages.out.banker.wallet.reject".localized("player" to player))
                        }
                        walletDB.setInspectorWallet(id, inspector)
                        walletDB.setVerificationWalletDate(id)
                    }
                    1 -> sender.sendMessage("localisation.messages.out.banker.wallet.already.open".localized())
                    -1 -> sender.sendMessage("localisation.messages.out.banker.wallet.already.reject".localized())
                    else -> sender.sendMessage("localisation.messages.out.error-search-data-in-database".localized())
                }
            }
        }
        return true
    }
}