package discord.dsbot

import data.Database
import functions.Functions
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordNotifierEvents(private val database: Database, private val discordBot: DiscordBot) : ListenerAdapter() {
    private val functions = Functions()
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            val componentId = event.componentId
            val parts = componentId.split(":")
            val action = parts[0]
            val walletId = parts[1].toInt()
            val verificationDatabase = database.getVerification(walletId)
            val discordUserID = event.user.id
            val uuid = database.getUUID(walletId).toString()
            val bankerPermission = "skybank.banker"
            println("Verification status for wallet ID \$walletId: \$verificationDatabase")

            // Проверка, было ли взаимодействие уже подтверждено
            if (event.isAcknowledged) {
                println("Interaction already acknowledged")
                return
            }

            event.deferEdit().queue()

            event.message.editMessageComponents().queue(
                { println("Компоненты сообщения успешно удалены") },
                { it.printStackTrace() }
            )
            val message = when (action) {
                "acceptAccount" -> {
                    if (verificationDatabase == 0) {
                        database.setVerification(walletId, 1)
                        database.setDeposit(walletId, "0")
                        database.setInspectorAccount(walletId,discordUserID)
                        database.setVerificationDate(walletId)
                        functions.sendMessageIsPlayerOnline(uuid, "Ваш запрос одобрили!")
                        functions.sendMessageIsPlayerHavePermission(uuid, bankerPermission, "Кошелек $walletId одобрил \${event.user.name}")
                        //TODO: event.user.name - переделать под игровой ник, а не дс
                        "Запрос был одобрен! (ID MESSAGE: `${event.messageId}`, ID ACCOUNT: `$walletId`)"
                    } else {
                        val discordIDInspector = database.getInspectorAccount(walletId) ?: return
                        val mentionInspector = discordBot.getMentionUser(discordIDInspector)
                        val verificationDate = database.getVerificationDate(walletId)
                        "Данный запрос уже был рассмотрен в игре! \n" +
                                "Рассмотрел - $mentionInspector\n" +
                                "Дата рассмотрения - `$verificationDate`"
                    }
                }
                "rejectAccount" -> {
                    if (verificationDatabase == 0) {
                        database.setVerification(walletId, -1)
                        database.setInspectorAccount(walletId,event.user.id)
                        database.setVerificationDate(walletId)
                        functions.sendMessageIsPlayerOnline(database.getUUID(walletId).toString(), "Ваш запрос отклонили!")
                        "Запрос был отклонен! (ID MESSAGE: `${event.messageId}`, ID ACCOUNT: `$walletId`)"
                    } else {
                        val discordIDInspector = database.getInspectorAccount(walletId) ?: return
                        val mentionInspector = discordBot.getMentionUser(discordIDInspector)
                        val verificationDate = database.getVerificationDate(walletId)
                        "Данный запрос уже был рассмотрен в игре! \n" +
                                "Рассмотрел - $mentionInspector\n" +
                                "Дата рассмотрения - `$verificationDate`"
                    }
                }
                else -> return
            }
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
