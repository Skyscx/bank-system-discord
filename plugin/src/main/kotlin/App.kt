

import bank.commands.BankerTumblerCommand
import bank.commands.ConvertDarkOnLightDiamondOre
import bank.commands.banker.WalletVerificationCommand
import bank.commands.tabcompleter.WalletsCommandCompleter
import bank.commands.tabcompleter.WalletsForceCommandCompleter
import bank.commands.transfers.TransferCommand
import bank.commands.wallets.ForceWalletCommands
import bank.commands.wallets.WalletCommands
import data.Config
import data.database.DatabaseManager
import data.database.collection.History
import data.database.collection.Reports
import data.database.collection.User
import data.database.collection.Wallet
import data.localisation.LocalisationManager
import data.managers.ActionDataManager
import data.managers.ConvertDataManager
import data.managers.TransferDataManager
import discord.FunctionsDiscord
import discord.dsbot.DiscordBot
import functions.events.PlayerConnection
import gui.convertdiamonds.ConvertDiamondsBlocksAmountInventory
import gui.convertdiamonds.ConvertDiamondsBlocksInventory
import gui.convertdiamonds.ConvertEvents
import gui.wallletmenu.WalletMenuInventoryEvent
import gui.wallletmenu.actionwallet.WalletActionsInventory
import gui.wallletmenu.actionwallet.WalletActionsInventoryEvent
import gui.wallletmenu.actionwallet.WalletHistoryInventory
import gui.wallletmenu.closewallet.WalletCloseInventoryEvent
import gui.wallletmenu.openwallet.WalletOpenInventoryEvent
import gui.wallletmenu.reportwallet.WalletReportInventoryEvent
import gui.wallletmenu.transferwallet.AddCommentInventory
import gui.wallletmenu.transferwallet.AmountPlayerInventory
import gui.wallletmenu.transferwallet.ConfirmTransferInventory
import gui.wallletmenu.transferwallet.TransferEvents
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.SQLException


lateinit var app: App


class App : JavaPlugin(), Listener {
    companion object {
        lateinit var instance: App
        lateinit var configPlugin: Config
        var discordBot: DiscordBot? = null
        lateinit var localizationManager: LocalisationManager
        lateinit var dbManager: DatabaseManager
        lateinit var walletDB: Wallet
        lateinit var userDB: User
        lateinit var historyDB: History
        lateinit var reportsDB: Reports

        fun String.localized(vararg replacements: Pair<String, String>): String {
            return localizationManager.getMessage(this, *replacements)
        }
    }

    override fun onEnable() {
        instance = this
        // Folder
        val pluginFolder = dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }

        // Config
        configPlugin = Config.getInstance(this)
        configPlugin.loadConfig()

        // Localisation
        localizationManager = LocalisationManager(this)
        val language = configPlugin.getString("locale")
        copyConfigFile("locales/messages_$language.yml")

        // Database
        val databaseFile = File(pluginFolder, "database.db")
        val url = "jdbc:sqlite:${databaseFile.absolutePath}"

        // Инициализация DatabaseManager
        try {
            dbManager = DatabaseManager.getInstance(url, this)
        } catch (e: SQLException) {
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        //Depends
        if (server.pluginManager.getPlugin("DiscordSRV") == null) {
            logger.severe("DiscordSRV plugin not found. Disabling plugin.")
            server.pluginManager.disablePlugin(this)
            return
        }

        // DiscordBot
        val token = configPlugin.getString("bot-token")
        if (token.isNullOrEmpty() || token == "your-bot-token-here") {
            logger.severe("Discord bot token is missing. Disabling plugin.")
            server.pluginManager.disablePlugin(this)
            return
        }

        discordBot = DiscordBot.getInstance(config)
        discordBot?.start(token)

        // Инициализация Database Collection
        val functionsDiscord = FunctionsDiscord()
        walletDB = Wallet.getInstance(dbManager, this, functionsDiscord)
        userDB = User.getInstance(dbManager, this, functionsDiscord)
        historyDB = History.getInstance(dbManager, this)
        reportsDB = Reports(dbManager, this)


        //Commands
        getCommand("wallet")?.setExecutor(WalletCommands(config))
        getCommand("wallet-force")?.setExecutor(ForceWalletCommands(config))
        getCommand("convert-diamonds")?.setExecutor(ConvertDarkOnLightDiamondOre())
        getCommand("wallet-verify")?.setExecutor(WalletVerificationCommand())
        getCommand("transfer")?.setExecutor(TransferCommand(config))
        getCommand("banker-tumbler")?.setExecutor(BankerTumblerCommand())


        Bukkit.getPluginManager().registerEvents(PlayerConnection(config), this)

        Bukkit.getPluginManager().registerEvents(WalletOpenInventoryEvent(config, discordBot!!), this)
        Bukkit.getPluginManager().registerEvents(WalletMenuInventoryEvent(), this)
        Bukkit.getPluginManager().registerEvents(WalletCloseInventoryEvent(config), this)
        Bukkit.getPluginManager().registerEvents(WalletReportInventoryEvent(), this)
        Bukkit.getPluginManager().registerEvents(WalletHistoryInventory(), this)

        val transferDataManager = TransferDataManager.instance
        val actionData = ActionDataManager.instance
        val convertData = ConvertDataManager.instance

        val amountPlayerInventory = AmountPlayerInventory(transferDataManager)
        val confirmTransferInventory = ConfirmTransferInventory(transferDataManager)
        val addComment = AddCommentInventory()

        val walletActionsInventory = WalletActionsInventory(actionData)

        val convertDiamondsBlocksInventory = ConvertDiamondsBlocksInventory(convertData)
        val convertDiamondsBlocksAmountInventory = ConvertDiamondsBlocksAmountInventory(convertData)
        Bukkit.getPluginManager().registerEvents(TransferEvents(amountPlayerInventory, confirmTransferInventory, addComment), this)
        Bukkit.getPluginManager().registerEvents(WalletActionsInventoryEvent(walletActionsInventory), this)
        Bukkit.getPluginManager().registerEvents(ConvertEvents(convertDiamondsBlocksAmountInventory, convertDiamondsBlocksInventory), this)


        // Tab Completer
        getCommand("wallet")?.tabCompleter = WalletsCommandCompleter()
        getCommand("wallet-force")?.tabCompleter = WalletsForceCommandCompleter()
    }

    override fun onDisable() {
        discordBot?.jda?.shutdownNow()
        dbManager.close()
        saveConfig()
    }

    override fun saveDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            saveResource("config.yml", false)
        }
    }
    fun getDiscordBot(): DiscordBot? {
        return discordBot
    }
    private fun copyConfigFile(resourcePath: String) {
        val pluginDirectory = dataFolder.toPath()
        val targetPath = pluginDirectory.resolve(resourcePath)

        if (!targetPath.toFile().exists()) {
            try {
                saveResource(resourcePath, false)
                logger.info("Successfully copied $resourcePath to $targetPath")
            } catch (e: Exception) {
                logger.severe("Failed to copy $resourcePath to $targetPath: ${e.message}")
            }
        } else {
            logger.info("$resourcePath already exists at $targetPath")
        }
    }

}