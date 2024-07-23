package discord

import github.scarsz.discordsrv.DiscordSRV
import java.util.*

class Functions {
    fun getPlayerDiscordID(uuid: UUID): String? {
        val discordId = DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid)
        return discordId
    }

}