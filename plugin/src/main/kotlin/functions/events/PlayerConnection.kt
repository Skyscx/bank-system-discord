package functions.events

import data.Database
import functions.Functions
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack


class PlayerConnection(private val database: Database) : Listener{
    private val functions = Functions()
    @EventHandler
    fun onPlayerConnect(event: PlayerJoinEvent){
        val player = event.player
        val playerUUID = player.uniqueId
        database.checkPlayerTask(playerUUID)
        val idList = database.getDepositIdsByUUID(playerUUID.toString())
        for (id in idList){
            if (database.isDepositAvailable(id)) {
                val deposit = database.getDeposit(id)
                val item = ItemStack(Material.DIAMOND_ORE) // TODO:Брать из конфигурации
                //val amount = 15 //TODO: Брать из конфигурации.
                functions.giveItem(player, item, deposit!!)
                database.deleteUserAccount(id)
            }
        }

    }


}