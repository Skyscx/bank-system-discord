import bank.commands.AddBalanceCommand
import bank.commands.BalanceCommand
import bank.commands.PayCommand
import bank.commands.SetBalanceCommand
import database.Database
import discord.DiscordSRVHook
import discord.dsbot.DiscordBot
import functions.events.PlayerConnection
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

        //Commands
        getCommand("pay")?.setExecutor(PayCommand(database))
        getCommand("balance")?.setExecutor(BalanceCommand(database))
        getCommand("add-balance")?.setExecutor(AddBalanceCommand(database))
        getCommand("set-balance")?.setExecutor(SetBalanceCommand(database))

        //Events
        val playerConnection = PlayerConnection(database)

        Bukkit.getPluginManager().registerEvents(playerConnection, this)

        //Depends
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.register()
        }

        //DiscordBot
        discordBot = DiscordBot(database)
        val token = config.getString("bot-token")
        discordBot.start(token)
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