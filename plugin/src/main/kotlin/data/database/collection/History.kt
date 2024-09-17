package data.database.collection

import App
import App.Companion.userDB
import data.database.DatabaseManager
import org.bukkit.Bukkit
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class History(private var dbManager: DatabaseManager, private var plugin: App) {

    /**
     * Создание записи о проделанной операции в таблицу с историей.
     * todo: 09/09/2024 - Добавить в параметры - Comment : String
     */

    fun insertBankHistory(
        typeOperation: String,
        oldBalance: Int = 0,
        newBalance: Int = 0,
        senderName: String,
        targetName: String,
        senderWalletID: Int,
        targetWalletID: Int,
        amount: Int,
        currency: String,
        status: Int,
        uuidSender: String,
        uuidTarget: String,
        comment: String) {
        plugin.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql = """
                    INSERT INTO bank_history(
                        TypeOperation, OldBalance, NewBalance, SenderIdWallet, TargetIdWallet, Amount, Currency, SenderName,
                        SenderUUID, SenderDiscordID, TargetName, TargetUUID, TargetDiscordID,
                        Date, Status, Comment
                    ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """.trimIndent()
                val discordSender = userDB.getDiscordIDbyUUID(uuidSender) ?: return@Runnable

                var discordTarget = "null"
                if (uuidTarget != "null"){
                    discordTarget = userDB.getDiscordIDbyUUID(uuidTarget) ?: return@Runnable
                }

                try {
                    dbManager.executeUpdate(sql,
                        typeOperation,
                        oldBalance,
                        newBalance,
                        senderWalletID,
                        targetWalletID,
                        amount,
                        currency,
                        senderName,
                        uuidSender,
                        discordSender,
                        targetName,
                        uuidTarget,
                        discordTarget,
                        currentDate,
                        status,
                        comment
                    )
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }
    }
    fun getUserHistory(userUUID: String, pageSize: Int, offset: Int): List<Map<String, Any>> {
        val query = """
        SELECT ID, SenderIdWallet, TargetIdWallet, Amount, Currency, SenderName, SenderUUID, SenderDiscordID,
               TargetName, TargetUUID, TargetDiscordID, Date, Status, Comment, TypeOperation, OldBalance, NewBalance
        FROM bank_history
        WHERE SenderUUID = ? OR TargetUUID = ?
        ORDER BY Date DESC
        LIMIT ? OFFSET ?
    """

        return dbManager.executeQuery(query, userUUID, userUUID, pageSize, offset)
    }

    companion object {
        @Volatile
        private var instance: History? = null

        fun getInstance(dbManager: DatabaseManager, plugin: App): History =
            instance ?: synchronized(this) {
                instance ?: History(dbManager, plugin).also { instance = it }
            }
    }
}