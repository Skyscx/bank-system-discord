package discord.dsbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.*
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.bukkit.configuration.file.FileConfiguration
import java.time.OffsetDateTime

class DiscordNotifier(configuration: FileConfiguration) {
    private val jda: JDA = DiscordBot.getJDA() ?: throw IllegalStateException("JDA is not initialized")
    private val channelIdLogger = configuration.getString("channel-id-logger") ?: "null"
    fun sendMessageChannel(channelId: String, message: String) {
        val channel = jda.getTextChannelById(channelId)
        if (channel != null) {
            channel.sendMessage(message).queue(
                { },
                { it.printStackTrace() }
            )
        } else {
            println("Channel $channelId not found.")
        }
    }
    fun sendMessageChannelLog(message: String) {
        val channel = jda.getTextChannelById(channelIdLogger)
        if (channel != null) {
            channel.sendMessage(message).queue(
                {  },
                { it.printStackTrace() }
            )
        } else {
            println("Channel with ID-LOGGER not found.")
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
    fun createEmbedMessage(
        url: String? = null,
        title: String? = null,
        description: String? = null,
        embedType: EmbedType? = EmbedType.UNKNOWN,
        timestamp: OffsetDateTime? = null,
        color: Int,
        thumbnail: Thumbnail? = null,
        siteProvider: Provider? = null,
        author: AuthorInfo? = null,
        videoInfo: VideoInfo? = null,
        footer: Footer? = null,
        image: ImageInfo? = null,
        fields: List<Field>? = null,
    ) : MessageEmbed{
        return MessageEmbed(
            url,
            title,
            description,
            embedType,
            timestamp,
            color,
            thumbnail,
            siteProvider,
            author,
            videoInfo,
            footer,
            image,
            fields
        )
    }
    fun sendEmbedMessageAndButtons(
        channelId: String,
        url: String? = null,
        title: String? = null,
        description: String? = null,
        embedType: EmbedType? = EmbedType.UNKNOWN,
        timestamp: OffsetDateTime? = null,
        color: Int,
        thumbnail: Thumbnail? = null,
        siteProvider: Provider? = null,
        author: AuthorInfo? = null,
        videoInfo: VideoInfo? = null,
        footer: Footer? = null,
        image: ImageInfo? = null,
        fields: List<Field>? = null,
        buttons: List<Button>
        ){
        val embedMessage = createEmbedMessage(
            url,
            title,
            description,
            embedType,
            timestamp,
            color,
            thumbnail,
            siteProvider,
            author,
            videoInfo,
            footer,
            image,
            fields
        )
        val channel = jda.getTextChannelById(channelId)
        channel?.sendMessage(MessageCreateBuilder().setEmbeds(embedMessage).setActionRow(buttons).build())?.queue()
    }

}