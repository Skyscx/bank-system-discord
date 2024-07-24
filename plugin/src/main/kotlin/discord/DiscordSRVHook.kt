package discord

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent
import org.bukkit.Bukkit

class DiscordSRVHook private constructor() {
    @Subscribe
    fun onMessageReceived(event: DiscordGuildMessageReceivedEvent) {
        val playerName = event.member.effectiveName
        val channel = event.channel.name
        val message = event.message.contentRaw

        Bukkit.broadcastMessage("[DS] : $playerName : $channel : $message")
    }

    companion object {
        private val instance = DiscordSRVHook()
        fun register() {
            DiscordSRV.api.subscribe(instance)
        }

        fun unregister() {
            DiscordSRV.api.unsubscribe(instance)
        }
    }
}
