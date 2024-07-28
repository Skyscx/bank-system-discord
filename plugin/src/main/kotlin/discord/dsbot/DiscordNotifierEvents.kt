package discord.dsbot

import data.Database
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordNotifierEvents(private val database: Database, private val discordBot: DiscordBot) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            val componentId = event.componentId
            val parts = componentId.split(":")
            val action = parts[0]
            val walletId = parts[1].toInt()
            println("Button interaction received: $componentId")
            println("Action: $action, Wallet ID: $walletId")

            val verificationDatabase = database.getVerification(walletId)
            val discordIDInspector = database.getInspectorAccount(walletId)
            if (discordIDInspector == null) {
                println("Inspector account is null for wallet ID: $walletId")
                return
            }
            val mentionInspector = discordBot.getMentionUser(discordIDInspector)
            val verificationDate = database.getVerificationDate(walletId)

            event.deferEdit().queue()

            val message = when (action) {
                "acceptAccount" -> {
                    if (verificationDatabase == 0) {
                        database.setVerification(walletId, 1)
                        database.setDeposit(walletId, "0")
                        "Запрос был одобрен! (ID MESSAGE: `${event.messageId}`, ID ACCOUNT: `$walletId`)"
                    } else {
                        "Данный запрос уже был рассмотрен в игре! \n" +
                                "Рассмотрел - $mentionInspector\n" +
                                "Дата рассмотрения - `$verificationDate`"
                    }
                }
                "rejectAccount" -> {
                    if (verificationDatabase == 0) {
                        database.setVerification(walletId, -1)
                        "Запрос был отклонен! (ID MESSAGE: `${event.messageId}`, ID ACCOUNT: `$walletId`)"
                    } else {
                        "Данный запрос уже был рассмотрен в игре! \n" +
                                "Рассмотрел - $mentionInspector\n" +
                                "Дата рассмотрения - `$verificationDate`"
                    }
                }
                else -> return
            }

            event.message.editMessageComponents().queue(
                { println("Компоненты сообщения успешно удалены") },
                { it.printStackTrace() }
            )

            event.message.editMessage(message).queue(
                { println("Сообщение отправлено") },
                { it.printStackTrace() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error processing button interaction: ${e.message}")
        }
    }
}
