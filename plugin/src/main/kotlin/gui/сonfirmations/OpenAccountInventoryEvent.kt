package gui.сonfirmations

import database.Database
import discord.FunctionsDiscord
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class OpenAccountInventoryEvent(private val database: Database) : Listener {
    private val functionsDiscord = FunctionsDiscord()
    @EventHandler
    fun OnClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.HOPPER) {
            if (e.view.title == "Подтверждение операции") {
                //open
                if (e.currentItem!!.itemMeta.displayName == "Подтвердить!") {
                    if (functionsDiscord.hasDiamondOre(player)){
                        val countAccounts = database.getAccountCount(player.uniqueId.toString())
                        if (countAccounts < 10){ //TODO: Сделать число из конфига.
                            database.insertAccount(player,"default")
                            functionsDiscord.sendMessagePlayer(player, "Банковский счет был успешно создан!")
                        }else{
                            functionsDiscord.sendMessagePlayer(player, "У вас уже максимальное количество счетов!")
                        }
                    }else{
                        functionsDiscord.sendMessagePlayer(player,"Недостаточно алмазной руды на руках!")
                    }
                }
                //close
                if (e.currentItem!!.itemMeta.displayName == "Отклонить!") {
                    functionsDiscord.sendMessagePlayer(player, "Отклонено.")
                }
                player.closeInventory()
                return
            }
        }
    }

}