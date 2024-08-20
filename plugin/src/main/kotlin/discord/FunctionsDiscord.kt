package discord

import github.scarsz.discordsrv.DiscordSRV
import java.util.*

class FunctionsDiscord() {
    //private val jda = discordBot.getJDA()
    fun getPlayerDiscordID(uuid: UUID): String? {
        val discordId = DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid)
        return discordId
    }
    //TODO: Если нету -> вернуть string "Null"



}