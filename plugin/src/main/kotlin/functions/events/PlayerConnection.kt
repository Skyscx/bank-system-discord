package functions.events

import database.Database
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerConnection(private val database: Database) : Listener{

    @EventHandler
    fun onPlayerConnect(event: PlayerJoinEvent){
        database.checkPlayerTask(event.player.uniqueId)
    }


}