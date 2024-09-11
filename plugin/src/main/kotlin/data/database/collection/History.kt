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
                        SenderIdWallet, TargetIdWallet, Amount, Currency, SenderName,
                        SenderUUID, SenderDiscordID, TargetName, TargetUUID, TargetDiscordID,
                        Date, Status, Comment
                    ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """.trimIndent()


                try {
                    dbManager.executeUpdate(sql,
                        senderWalletID, targetWalletID, amount, currency, senderName,
                        uuidSender, userDB.getDiscordIDbyUUID(uuidSender) as Any,
                        targetName, uuidTarget, userDB.getDiscordIDbyUUID(uuidTarget) as Any,
                        currentDate, status, comment
                    )
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }
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