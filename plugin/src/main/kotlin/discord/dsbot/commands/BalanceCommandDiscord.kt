package discord.dsbot.commands

import App.Companion.userDB
import App.Companion.walletDB
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.configuration.file.FileConfiguration

class BalanceCommandDiscord(config: FileConfiguration) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "balance") return
        val user = event.user
        val uuidSenderFuture = userDB.getUUIDbyDiscordID(user.id)
        uuidSenderFuture.thenAccept { uuidSender ->
            if (uuidSender == null) {
                event.reply("Ваш игровой аккаунт не привязан к учетной записи банка.").queue()
                return@thenAccept
            }
            val walletID = userDB.getDefaultWalletByUUID(uuidSender) ?: return@thenAccept
            val balance = walletDB.getWalletBalance(walletID)

            event.reply("Ваш баланс: $balance").setEphemeral(true).queue()
        }
        return
    }

}