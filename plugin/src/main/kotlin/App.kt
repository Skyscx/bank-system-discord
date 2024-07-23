import bank.commands.AddBalanceCommand
import bank.commands.BalanceCommand
import bank.commands.PayCommand
import bank.commands.SetBalanceCommand
import database.Database
import discord.DiscordSRVHook
import functions.events.PlayerConnection
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.sql.SQLException


lateinit var app: App


class App : JavaPlugin(), Listener {
    private var database: Database? = null

    override fun onEnable() {
        //Folder
        val pluginFolder = dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }

        //Database
        try {
            database = Database(dataFolder, this)
        } catch (e: SQLException) {
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        //Commands
        getCommand("pay")?.setExecutor(PayCommand())
        getCommand("balance")?.setExecutor(BalanceCommand())
        getCommand("add-balance")?.setExecutor(AddBalanceCommand())
        getCommand("set-balance")?.setExecutor(SetBalanceCommand())

        //Events
        val playerConnection = PlayerConnection(database!!)

        Bukkit.getPluginManager().registerEvents(playerConnection, this)

        //Depends
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.register()
        }
    }

    override fun onDisable() {
        if (server.pluginManager.getPlugin("DiscordSRV") != null){
            DiscordSRVHook.unregister()
        }
        database?.closeConnection();

    }


}