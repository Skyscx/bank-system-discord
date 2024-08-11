

import bank.commands.accounts.AccountCommands
import bank.commands.tabcompleter.AccountsCommandCompleter
import data.Config
import data.Database
import data.localisation.LocalisationManager
import discord.DiscordSRVHook
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
        lateinit var database: Database
        lateinit var localizationManager: LocalisationManager
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
        // Localisation
        localizationManager = LocalisationManager(this)
        // Database
        val databaseFolder = File(dataFolder, "data")
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs()
        }
        val databaseFile = File(databaseFolder, "database.db")
        val url = "jdbc:sqlite:${databaseFile.absolutePath}"
        try {
            database = Database.getInstance(url,this)
        } catch (e: SQLException) {
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        // DiscordBot
        val discordBot = DiscordBot.getInstance(database, config)
        val token = config.getString("bot-token")
        discordBot.start(token)
        // Classes



        //Commands
        getCommand("account")?.setExecutor(AccountCommands(database))

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
        Bukkit.getPluginManager().registerEvents(PlayerConnection(database), this)
        Bukkit.getPluginManager().registerEvents(AccountOpenInventoryEvent(database, config, discordBot), this)
        //todo: 07/08/2024 21/10 переделать команды, сделать локализацию, попробовать очистить ветку main
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
        database.closeConnection();
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


}