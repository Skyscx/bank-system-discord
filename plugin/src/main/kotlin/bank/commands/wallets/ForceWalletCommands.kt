package bank.commands.wallets

import App.Companion.localizationManager
import bank.commands.wallets.collectionforce.RemoveForceCommandHandler
import bank.commands.wallets.collectionforce.WalletBalanceAddForceHandler
import bank.commands.wallets.collectionforce.WalletBalanceRemoveForceHandler
import discord.dsbot.DiscordBot
import functions.Functions
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

class ForceWalletCommands(private val config: FileConfiguration, private val discordBot: DiscordBot) : CommandExecutor {
    //wallet-force
    private val functions = Functions()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val isPlayer = functions.senderIsPlayer(sender)
        if (!isPlayer.second) {
            sender.sendMessage(isPlayer.first)
            return true
        }
        if (!functions.hasPermission(sender, "skybank.banker")) { //TODO: Проверить права TESTING
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.no-permissions"))
            return true
        }
        if (args.isEmpty()) {
            //TODO: Сделать открытие меню инвентаря со всеми доступными функциями.
            sender.sendMessage(localizationManager.getMessage("localisation.messages.out.developing"))
            return true
        }
        val argsArray = args.toList().toTypedArray()
        when (args[0].lowercase()) {
            "remove" -> {
                val removeForceCommandHandler = RemoveForceCommandHandler(config, discordBot)
                removeForceCommandHandler.handleRemoveForceCommand(sender, argsArray)
            }
            "balance" -> {
                when (args[1].lowercase()) {
                    "add" -> {
                        val walletBalanceAddForceHandler = WalletBalanceAddForceHandler(config, discordBot)
                        walletBalanceAddForceHandler.handleBalanceAddForceCommand(sender, argsArray)
                    }
                    "remove" -> {
                        val walletBalanceRemoveForceHandler = WalletBalanceRemoveForceHandler(config, discordBot)
                        walletBalanceRemoveForceHandler.handleBalanceRemoveForceCommand(sender, argsArray)
                    }
                    else -> {
                        functions.unknownCommand(sender) //todo: переделать на сообщение о том что add или remove
                    }
                }
                return true
            }
            else -> {
                functions.unknownCommand(sender)
            }
        }
        return true
    }
}