import bank.commands.AddBalanceCommand
import bank.commands.BalanceCommand
import bank.commands.PayCommand
import bank.commands.SetBalanceCommand
import database.Database
import discord.DiscordSRVHook
import discord.dsbot.DiscordBot
import functions.events.PlayerConnection
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.SQLException


lateinit var app: App


class App : JavaPlugin(), Listener {
    lateinit var database: Database
    private lateinit var discordBot: DiscordBot


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

        //discord.dsbot.DiscordBot
        discordBot = DiscordBot(database)
        discordBot.start("MTI2NTAwMjcyMTQ4ODkyODgyOQ.GtnVS0.QhQF26tObwGDt2EDLdNqQl5rMxMeumn6p0XXJI")
    }

    override fun onDisable() {
        discordBot.jda.shutdown()
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.unregister()
        }
        database.closeConnection();
    }


}