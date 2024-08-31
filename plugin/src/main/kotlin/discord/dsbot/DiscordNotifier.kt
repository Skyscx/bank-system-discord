package discord.dsbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class DiscordNotifier(private val jda: JDA) {
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!

    fun sendMessageChannel(channelId: String, message: String) {
        val channel = jda.getTextChannelById(channelId)
        if (channel != null) {
            channel.sendMessage(message).queue(
                { println("Сообщение отправлено: $message") },
                { it.printStackTrace() }
            )
        } else {
            println("Канал с ID \$channelId не найден.")
        }
    }

    fun sendPrivateMessage(userId: String, message: String) {
        jda.awaitReady()
        val user: User? = jda.retrieveUserById(userId).complete()
        if (user != null) {
            user.openPrivateChannel().queue { channel ->
                channel.sendMessage(message).queue()
            }
        } else {
            println("User with ID $userId not found.")
        }
    }

    fun sendMessageWithButtons(channelId: String, message: String, walletId: String) {
        val channel = jda.getTextChannelById(channelId)
        if (channel != null) {
            channel.sendMessage(message).addActionRow(
                Button.of(ButtonStyle.SUCCESS, "acceptAccount:$walletId", "Принять"),
                Button.of(ButtonStyle.DANGER, "rejectAccount:$walletId", "Отклонить")
            ).queue(
                { println("Сообщение с кнопками отправлено: $message") },
                { it.printStackTrace() }
            )
        } else {
            println("Канал с ID $channelId не найден.")
        }
    }
}