package data.database.collection

import App
import data.database.DatabaseManager
import org.bukkit.Bukkit
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class Reports(private var dbManager: DatabaseManager, private var plugin: App) {

    /**
     * ID - идентификатор +
     * SenderName - Игровое имя MC +
     * SenderUUID - Идентификатор игрока MC +
     * SenderDID - Идентификатор игрока в Discord +
     * DateDispatch - Время и дата когда было отправлено (!) - тут реализовать
     * Type - Тип жалобы (Подробнее в команде wallet report)  +(reason)
     * Reason - Текст жалобы +(message)
     * From - Откуда поступила - MC | Discord +(Напрямую вводить "MC")
     * Inspector - Кто закрыт жалобу (Не нужно вводить)
     * ResponseType - Тип закрытия жалобы (Не нужно вводить)
     * ResponseText - Текст закрытия жалобы (Не нужно вводить)
     * DateResponse - Дата закрытия жалобы (Не нужно вводить)
     * Status - Переменная для проверки открыта или закрыта жалоба. [1 - открыта | 0 - закрыта] +(Напрямую вводить "1")
     */
    fun insertBankReport(
        senderName: String,
        senderUUID: String,
        senderDID: String,
        type: String,
        reason: String,
        from: String,
        reportID: Int,
        inspector: String = "unavailable",
        responseType: String = "unavailable",
        responseText: String = "unavailable",
        dateResponse: String = "unavailable",
        ) {
        plugin.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val dateDispatch = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql = """
                    INSERT INTO bank_reports(
                        SenderName, 
                        SenderUUID, 
                        SenderDID, 
                        DateDispatch, 
                        Type,
                        Reason, 
                        FromAddress, 
                        ReportID,
                        Inspector,
                        ResponseType,
                        ResponseText,
                        DateResponse,
                        Status
                    ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """.trimIndent()

                try {
                    dbManager.executeUpdate(sql,
                        senderName, senderUUID, senderDID, dateDispatch, type, reason, from, reportID, inspector, responseType, responseText, dateResponse, 1
                    )
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }
    }

    /**
     * Получение статуса записи в таблице bank_reports
     */
    fun getStatusBankReport(id: Int): Int? {
        val sql = "SELECT Status FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            return row?.get("Status") as? Int
        }
        return null
    }

    /**
     * Присвоение статуса записи в таблице bank_reports
     */
    fun setStatusBankReport(id: Int, status: Int): Boolean {
        val sql = "UPDATE bank_reports SET Status = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, status, id)
    }

    /**
     * Получение имени отправителя по ID записи из таблицы bank_reports
     */
    fun                                                                                                                getSenderName(id: Int): String? {
        val sql = "SELECT SenderName FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("SenderName") as? String
    }

    /**
     * Присвоение имени отправителя по ID записи в таблице bank_reports
     */
    fun setSenderName(id: Int, senderName: String): Boolean {
        val sql = "UPDATE bank_reports SET SenderName = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, senderName, id)
    }

    /**
     * Получение UUID отправителя по ID записи из таблицы bank_reports
     */
    fun getSenderUUID(id: Int): String? {
        val sql = "SELECT SenderUUID FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("SenderUUID") as? String
    }

    /**
     * Присвоение UUID отправителя по ID записи в таблице bank_reports
     */
    fun setSenderUUID(id: Int, senderUUID: String): Boolean {
        val sql = "UPDATE bank_reports SET SenderUUID = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, senderUUID, id)
    }

    /**
     * Получение Discord ID отправителя по ID записи из таблицы bank_reports
     */
    fun getSenderDID(id: Int): String? {
        val sql = "SELECT SenderDID FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("SenderDID") as? String
    }

    /**
     * Присвоение Discord ID отправителя по ID записи в таблице bank_reports
     */
    fun setSenderDID(id: Int, senderDID: String): Boolean {
        val sql = "UPDATE bank_reports SET SenderDID = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, senderDID, id)
    }

    /**
     * Получение даты отправки по ID записи из таблицы bank_reports
     */
    fun getDateDispatch(id: Int): String? {
        val sql = "SELECT DateDispatch FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("DateDispatch") as? String
    }

    /**
     * Присвоение даты отправки по ID записи в таблице bank_reports
     */
    fun setDateDispatch(id: Int, dateDispatch: String): Boolean {
        val sql = "UPDATE bank_reports SET DateDispatch = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, dateDispatch, id)
    }

    /**
     * Получение типа по ID записи из таблицы bank_reports
     */
    fun getType(id: Int): String? {
        val sql = "SELECT Type FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("Type") as? String
    }

    /**
     * Присвоение типа по ID записи в таблице bank_reports
     */
    fun setType(id: Int, type: String): Boolean {
        val sql = "UPDATE bank_reports SET Type = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, type, id)
    }

    /**
     * Получение причины по ID записи из таблицы bank_reports
     */
    fun getReason(id: Int): String? {
        val sql = "SELECT Reason FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("Reason") as? String
    }

    /**
     * Присвоение причины по ID записи в таблице bank_reports
     */
    fun setReason(id: Int, reason: String): Boolean {
        val sql = "UPDATE bank_reports SET Reason = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, reason, id)
    }

    /**
     * Получение источника по ID записи из таблицы bank_reports
     */
    fun getFromAddress(id: Int): String? {
        val sql = "SELECT FromAddress FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("FromAddress") as? String
    }

    /**
     * Присвоение источника по ID записи в таблице bank_reports
     */
    fun setFromAddress(id: Int, from: String): Boolean {
        val sql = "UPDATE bank_reports SET FromAddress = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, from, id)
    }

    /**
     * Получение инспектора по ID записи из таблицы bank_reports
     */
    fun getInspector(id: Int): String? {
        val sql = "SELECT Inspector FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("Inspector") as? String
    }

    /**
     * Присвоение инспектора по ID записи в таблице bank_reports
     */
    fun setInspector(id: Int, inspector: String): Boolean {
        val sql = "UPDATE bank_reports SET Inspector = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, inspector, id)
    }

    /**
     * Получение типа ответа по ID записи из таблицы bank_reports
     */
    fun getResponseType(id: Int): String? {
        val sql = "SELECT ResponseType FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("ResponseType") as? String
    }

    /**
     * Присвоение типа ответа по ID записи в таблице bank_reports
     */
    fun setResponseType(id: Int, responseType: String): Boolean {
        val sql = "UPDATE bank_reports SET ResponseType = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, responseType, id)
    }

    /**
     * Получение текста ответа по ID записи из таблицы bank_reports
     */
    fun getResponseText(id: Int): String? {
        val sql = "SELECT ResponseText FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("ResponseText") as? String
    }

    /**
     * Присвоение текста ответа по ID записи в таблице bank_reports
     */
    fun setResponseText(id: Int, responseText: String): Boolean {
        val sql = "UPDATE bank_reports SET ResponseText = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, responseText, id)
    }

    /**
     * Получение даты ответа по ID записи из таблицы bank_reports
     */
    fun getDateResponse(id: Int): String? {
        val sql = "SELECT DateResponse FROM bank_reports WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("DateResponse") as? String
    }

    /**
     * Присвоение даты ответа по ID записи в таблице bank_reports
     */
    fun setDateResponse(id: Int, dateResponse: String): Boolean {
        val sql = "UPDATE bank_reports SET DateResponse = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, dateResponse, id)
    }

    /**
     * Генерация уникального 12-значного числа и проверка его наличия в базе данных /todo: перенести во функции
     */
    fun generateUniqueReportID(): Int {
        var uniqueNumber: Int
        do {
            uniqueNumber = Random.nextInt(10000000, 99999999)
        } while (doesReportIDExist(uniqueNumber))
        return uniqueNumber
    }

    /**
     * Проверка существования числа в базе данных
     */
    private fun doesReportIDExist(number: Int): Boolean {
        val sql = "SELECT 1 FROM bank_reports WHERE reportID = ?"
        val result = dbManager.executeQuery(sql, number)
        return result.isNotEmpty()
    }

    /**
     * Получение значения ID по reportID из таблицы bank_reports
     */
    fun getID(reportID: Int): Int? {
        val sql = "SELECT ID FROM bank_reports WHERE reportID = ?"
        val result = dbManager.executeQuery(sql, reportID)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            return row?.get("ID") as? Int
        }
        return null
    }

    /**
     * Присвоение значения reportID по ID записи в таблице bank_reports todo: remove
     */
    fun setReportID(id: Int, reportID: Int): Boolean {
        val sql = "UPDATE bank_reports SET reportID = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, reportID, id)
    }

    companion object {
        @Volatile
        private var instance: Reports? = null

        fun getInstance(dbManager: DatabaseManager, plugin: App): Reports =
            instance ?: synchronized(this) {
                instance ?: Reports(dbManager, plugin).also { instance = it }
            }
    }
}