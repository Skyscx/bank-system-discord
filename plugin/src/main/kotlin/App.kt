
import bank.commands.*
import bank.commands.banker.AccountVerificationCommand
import database.Database
import discord.DiscordSRVHook
import discord.dsbot.DiscordBot
import functions.events.PlayerConnection
import gui.сonfirmations.OpenAccountInventoryEvent
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.SQLException


lateinit var app: App


class App : JavaPlugin(), Listener {
    lateinit var database: Database
    private lateinit var discordBot: DiscordBot
    private lateinit var config: FileConfiguration



    override fun onEnable() {
        //Folder
        val pluginFolder = dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }

        //Database
        val databaseFolder = File(dataFolder, "database")
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs()
        }
        val databaseFile = File(databaseFolder, "database.db")
        val url = "jdbc:sqlite:${databaseFile.absolutePath}"
        try {
            database = Database(url, this)
        } catch (e: SQLException) {
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }
        //Config
        saveDefaultConfig()
        reloadConfig()
        config = getConfig()
        //DiscordBot
        discordBot = DiscordBot(database, config)
        val token = config.getString("bot-token")
        discordBot.start(token)
        //Commands
        getCommand("pay")?.setExecutor(PayCommand(database))
        getCommand("balance")?.setExecutor(BalanceCommand(database))
        getCommand("add-balance")?.setExecutor(BalanceAddCommand(database))
        getCommand("set-balance")?.setExecutor(BalanceSetCommand(database))
        getCommand("open-account")?.setExecutor(AccountOpenCommand())
        getCommand("account-set-name")?.setExecutor(AccountSetNameCommand(database))
        getCommand("account-verify")?.setExecutor(AccountVerificationCommand(database))
        getCommand("bank-reload-plugin")?.setExecutor(PluginReloadCommand(this))

        //accounts-list
        //transfer-account-id
        //transfer-account-name
        //transfer-account-default
        //account-set-default
        //account-history
        //bank-history
        //account-close

        //Events
        Bukkit.getPluginManager().registerEvents(PlayerConnection(database), this)
        Bukkit.getPluginManager().registerEvents(OpenAccountInventoryEvent(database, config, discordBot), this)

        //Depends
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.register()
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



}