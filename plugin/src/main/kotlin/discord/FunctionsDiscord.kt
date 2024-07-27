package discord

import discord.dsbot.DiscordBot
import github.scarsz.discordsrv.DiscordSRV
import java.util.*

class FunctionsDiscord(discordBot: DiscordBot) {
    private val jda = discordBot.getJDA()
    fun getPlayerDiscordID(uuid: UUID): String? {
        val discordId = DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid)
        return discordId
    }

    fun mentionUserById(userId: String): String? {
        val user = jda.getUserById(userId)
        return if (user != null) {
            user.asMention
        } else {
            null
        }
    }


}