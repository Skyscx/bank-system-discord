package events

import database.Database
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerConnection(private val database: Database) : Listener {

    @EventHandler
    fun onPlayerConnect(event: PlayerJoinEvent){
        database.checkPlayer(event.player.uniqueId)
    }
}