package discord.dsbot

import database.Database
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordNotifierEvents(private val database: Database) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val componentId = event.componentId
        val parts = componentId.split(":")
        val action = parts[0]
        val walletId = parts[1].toInt()
        val verificationDatabase = database.getVerification(walletId)
        event.deferEdit().queue()
        when (action) {
            "acceptAccount" -> {
                event.message.editMessageComponents().queue(
                    { println("Компоненты сообщения успешно удалены") },
                    { it.printStackTrace() }
                )
                if (verificationDatabase == 0) {
                    event.message.editMessage("Запрос был одобрен! (ID MESSAGE: ${event.messageId}, ID ACCOUNT: $walletId)").queue(
                        { println("Сообщение одобрения отправлено") },
                        { it.printStackTrace() }
                    )
                    database.setVerification(walletId, 1)
                    database.setDeposit(walletId, 0.toString())
                } else {
                    event.message.editMessage("Данный запрос уже был рассмотрен в игре! \n" +
                            "Рассмотрел - #UNKOWN\n" +
                            "Дата рассмотрения - #UNKOWN").queue(
                        { println("Сообщение об уже рассмотренном запросе отправлено") },
                        { it.printStackTrace() }
                    )
                }
            }
            "rejectAccount" -> {
                event.message.editMessageComponents().queue(
                    { println("Компоненты сообщения успешно удалены") },
                    { it.printStackTrace() }
                )
                if (verificationDatabase == 0) {
                    event.message.editMessage("Запрос был отклонен! (ID MESSAGE: ${event.messageId}, ID ACCOUNT: $walletId)").queue(
                        { println("Сообщение отклонения отправлено") },
                        { it.printStackTrace() }
                    )
                    database.setVerification(walletId, -1)
                } else {
                    event.message.editMessage("Данный запрос уже был рассмотрен в игре! \n" +
                            "Рассмотрел - #UNKOWN\n" +
                            "Дата рассмотрения - #UNKOWN").queue(
                        { println("Сообщение об уже рассмотренном запросе отправлено") },
                        { it.printStackTrace() }
                    )
                }
            }
        }
    }
}
