

import bank.commands.accounts.AccountCommands
import bank.commands.tabcompleter.AccountsCommandCompleter
import data.Config
import data.database.DatabaseManager
import data.database.collection.History
import data.database.collection.User
import data.database.collection.Wallet
import data.localisation.LocalisationManager
import discord.DiscordSRVHook
import discord.FunctionsDiscord
import discord.dsbot.DiscordBot
import functions.events.PlayerConnection
import gui.accountmenu.openaccount.AccountOpenInventoryEvent
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.SQLException


lateinit var app: App


class App : JavaPlugin(), Listener {
    companion object {
        lateinit var configPlugin: Config
        lateinit var discordBot: DiscordBot
        //lateinit var database: Database
        lateinit var localizationManager: LocalisationManager

        lateinit var dbManager: DatabaseManager
        lateinit var walletDB: Wallet
        lateinit var userDB: User
        lateinit var historyDB: History

    }



    override fun onEnable() {
        // Folder
        val pluginFolder = dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }
        // Config
        configPlugin = Config.getInstance(this)
        configPlugin.loadConfig()
        // Database
        /**
         * Создание папки "data" - deleted
         */
//        val databaseFolder = File(dataFolder, "data")
//        if (!databaseFolder.exists()) {
//            databaseFolder.mkdirs()
//        }
        val databaseFile = File(pluginFolder, "database.db")
        val url = "jdbc:sqlite:${databaseFile.absolutePath}"

        /**
         * Старая версия базы данных
         */
//        try {
//            database = Database.getInstance(url,this)
//        } catch (e: SQLException) {
//            e.printStackTrace()
//            server.pluginManager.disablePlugin(this)
//            return
//        }
        // Инициализация DatabaseManager
        try {
            dbManager = DatabaseManager.getInstance(url, this)
        } catch (e: SQLException) {
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }
        // DiscordBot
        val discordBot = DiscordBot.getInstance(dbManager, config)
        val token = config.getString("bot-token")
        discordBot.start(token)
        // Инициализация Database Collection
        val functionsDiscord = FunctionsDiscord()
        walletDB = Wallet.getInstance(dbManager, this, functionsDiscord)
        userDB = User.getInstance(dbManager, this, functionsDiscord)
        historyDB = History.getInstance(dbManager, this)

        // Localisation
        localizationManager = LocalisationManager(this)
        copyConfigFile("locales/messages_en.yml")

        //Commands
        // TODO: Временно переделано!!! Тестирование dbManager
        getCommand("account")?.setExecutor(AccountCommands())
        // TODO: Временно переделано!!! Тестирование dbManager

        //getCommand("pay")?.setExecutor(PayCommand(database))
        //getCommand("balance")?.setExecutor(BalanceCommand(database))
        //getCommand("add-balance")?.setExecutor(BalanceAddCommand(database))
        //getCommand("set-balance")?.setExecutor(BalanceSetCommand(database))
        //getCommand("open-account")?.setExecutor(AccountOpenCommand())
        //getCommand("account-set-name")?.setExecutor(AccountSetNameCommand(database))
        //getCommand("account-verify")?.setExecutor(AccountVerificationCommand(database))
        //getCommand("account-remove")?.setExecutor(AccountRemoveCommand(database))
        //getCommand("transfer")?.setExecutor(NewTransferCommand(database))
        //getCommand("account-set-default-wallet")?.setExecutor(AccountSetDefaultWalletCommand(database))
        //getCommand("account-renaming")?.setExecutor(Events())
        //getCommand("bank-reload-plugin")?.setExecutor(PluginReloadCommand(this))

        //accounts-list
        //transfer-account-id
        //transfer-account-name
        //transfer-account-default
        //account-set-default
        //account-history
        //bank-history
        //account-close

        //gui.accountmenu.renamingaccount.anviltest.Events

        // TODO: Временно переделано!!! Тестирование dbManager
        Bukkit.getPluginManager().registerEvents(PlayerConnection(), this)
        Bukkit.getPluginManager().registerEvents(AccountOpenInventoryEvent(config, discordBot), this)
        // TODO: Временно переделано!!! Тестирование dbManager

        //todo: 07/08/2024 21/10 переделать команды, сделать локализацию
        //server.pluginManager.registerEvents(AccountRenamingInventoryEvent(), this)

        // Tab Completer
        getCommand("account")?.tabCompleter = AccountsCommandCompleter()

        //Depends
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.register()
            //todo: сделать выключение плагина если плагин не найден.
        }


    }
    //

    override fun onDisable() {
        discordBot.jda.shutdown()
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.unregister()
        }
        dbManager.close()
        // TODO: Временно переделано!!! Тестирование dbManager
        //database.closeConnection()
        // TODO: Временно переделано!!! Тестирование dbManager
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
    fun getDiscordBot(): DiscordBot {
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