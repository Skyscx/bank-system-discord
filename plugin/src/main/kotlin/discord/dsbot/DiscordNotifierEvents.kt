package discord.dsbot

import App.Companion.discordBot
import App.Companion.walletDB
import functions.Functions
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordNotifierEvents: ListenerAdapter() {

    private val functions = Functions()
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!
    //todo: сделать сообщение из конфига!!!!

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            val componentId = event.componentId
            val parts = componentId.split(":")
            val action = parts[0]
            val walletId = parts[1].toInt()
            val verificationDatabase = walletDB.getVerificationWallet(walletId)
            val discordUserID = event.user.id
            val uuid = walletDB.getUUIDbyWalletID(walletId)
            val bankerPermission = "skybank.banker" // todo: както подругому придумать, например через Functions
            val status = when (verificationDatabase) {
                1 -> "Запрос был одобрен."
                -1 -> "Запрос был отклонен."
                else -> "Статус запроса неизвестен."
            }
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
                        walletDB.setVerificationWallet(walletId, 1)
                        walletDB.setDepositWallet(walletId, "0")
                        walletDB.setInspectorWallet(walletId,discordUserID)
                        walletDB.setVerificationWalletDate(walletId)
                        functions.sendMessageIsPlayerOnline(uuid!!, "Ваш запрос одобрили!")
                        functions.sendMessageIsPlayerHavePermission(uuid, bankerPermission, "Кошелек $walletId одобрил \${event.user.name}")
                        //TODO: event.user.name - переделать под игровой ник, а не дс
                        "Запрос был одобрен! (ID MESSAGE: `${event.messageId}`, ID ACCOUNT: `$walletId`)"
                    } else {
                        val discordIDInspector = walletDB.getInspectorWallet(walletId) ?: return
                        val mentionInspector = discordBot?.getMentionUser(discordIDInspector)
                        val verificationDate = walletDB.getVerificationWalletDate(walletId)
                        "Данный запрос уже был рассмотрен в игре! \n" +
                                "Рассмотрел - $mentionInspector\n" +
                                "Дата рассмотрения - `$verificationDate`" +
                                "\n" +
                                status
                    }
                }
                "rejectAccount" -> {
                    if (verificationDatabase == 0) {
                        walletDB.setVerificationWallet(walletId, -1)
                        walletDB.setInspectorWallet(walletId,event.user.id)
                        walletDB.setVerificationWalletDate(walletId)
                        functions.sendMessageIsPlayerOnline(walletDB.getUUIDbyWalletID(walletId).toString(), "Ваш запрос отклонили!")
                        "Запрос был отклонен! (ID MESSAGE: `${event.messageId}`, ID ACCOUNT: `$walletId`)"
                    } else {
                        val discordIDInspector = walletDB.getInspectorWallet(walletId) ?: return
                        val mentionInspector = discordBot?.getMentionUser(discordIDInspector)
                        val verificationDate = walletDB.getVerificationWalletDate(walletId)
                        "Данный запрос уже был рассмотрен в игре! \n" +
                                "Рассмотрел - $mentionInspector\n" +
                                "Дата рассмотрения - `$verificationDate`" +
                                "\n" +
                                status
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
