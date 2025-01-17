package data.database

import App
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class DatabaseManager private constructor(url: String, private val plugin: App) {

    private var connection: Connection? = null

    init {
        try {
            connect(url)
            createTableUsers()
            createTableWallets()
            createTableHistory()
            createTableReports()
        } catch (e: SQLException) {
            e.printStackTrace()
            plugin.logger.severe("Failed to initialize database: ${e.message}")
            plugin.server.pluginManager.disablePlugin(plugin)
        }
    }

    private fun connect(url: String) {
        try {
            connection = DriverManager.getConnection(url)
            plugin.logger.info("Successfully connected to the database.")
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to connect to the database: ${e.message}")
            throw e
        }
    }

    // Обновление данных
    fun executeUpdate(query: String, vararg args: Any): Boolean {
        return try {
            val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
            for (i in args.indices) {
                preparedStatement.setObject(i + 1, args[i])
            }
            val rowsAffected = preparedStatement.executeUpdate()
            rowsAffected > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }



    // Получение данных
    fun executeQuery(query: String, vararg args: Any): List<Map<String, Any>> {
        val result = mutableListOf<Map<String, Any>>()
        try {
            val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
            for (i in args.indices) {
                preparedStatement.setObject(i + 1, args[i])
            }
            val resultSet = preparedStatement.executeQuery()
            val metaData = resultSet.metaData
            val columnCount = metaData.columnCount

            while (resultSet.next()) {
                val row = mutableMapOf<String, Any>()
                for (i in 1..columnCount) {
                    val columnName = metaData.getColumnName(i)
                    val value = resultSet.getObject(i)
                    if (value != null) {
                        row[columnName] = value
                    }
                }
                result.add(row)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return result
    }

    private fun createTable(query: String) {
        try {
            val statement = connection!!.createStatement()
            statement.execute(query)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            connection?.close()
            plugin.logger.info("Database connection closed.")
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun createTableUsers() {
        val sql = """
        CREATE TABLE IF NOT EXISTS bank_users (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            PlayerName TEXT NOT NULL,
            UUID TEXT NOT NULL,
            DiscordID TEXT NOT NULL,
            ActivatedBank INTEGER NOT NULL,
            Registration TEXT NOT NULL,
            `2f Auth` INTEGER NOT NULL,
            PrivateKey TEXT NOT NULL,
            LastOperation TEXT NOT NULL,
            USDT INTEGER NOT NULL,
            Level INTEGER NOT NULL,
            DefaultWalletID INTEGER NOT NULL,
            Wallet1 INTEGER NOT NULL,
            Wallet2 INTEGER NOT NULL,
            Wallet3 INTEGER NOT NULL,
            Wallet4 INTEGER NOT NULL,
            Wallet5 INTEGER NOT NULL,
            Style TEXT NOT NULL
        );
    """.trimIndent()
        createTable(sql)
    }

    private fun createTableWallets() {
        val sql = """
            CREATE TABLE IF NOT EXISTS bank_wallets (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                TYPE TEXT NOT NULL,
                UUID TEXT NOT NULL,
                DiscordID TEXT NOT NULL,
                Registration TEXT NOT NULL,
                PrivateKey TEXT NOT NULL,
                Balance INTEGER NOT NULL,
                Currency TEXT NOT NULL,
                Name TEXT NOT NULL,
                Verification INTEGER NOT NULL,
                Deposit INTEGER NOT NULL,
                Inspector TEXT NOT NULL,
                VerificationDate TEXT NOT NULL,
                Status INTEGER NOT NULL,
                Tariff INTEGER NOT NULL
                );
        """.trimIndent()
        createTable(sql)
    }

    private fun createTableHistory() {
        val sql = """
            CREATE TABLE IF NOT EXISTS bank_history (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                TypeOperation TEXT NOT NULL,
                OldBalance INTEGER NOT NULL,
                NewBalance INTEGER NOT NULL,
                SenderIdWallet INTEGER NOT NULL,
                TargetIdWallet INTEGER NOT NULL,
                Amount INTEGER NOT NULL,
                Currency TEXT NOT NULL,
                SenderName TEXT NOT NULL,
                SenderUUID TEXT NOT NULL,
                SenderDiscordID TEXT NOT NULL,
                TargetName TEXT NOT NULL,
                TargetUUID TEXT NOT NULL,
                TargetDiscordID TEXT NOT NULL,
                Date TEXT NOT NULL,
                Status INTEGER NOT NULL,
                Comment TEXT NOT NULL
                );
        """.trimIndent()
        createTable(sql)
    }

    /**
     * ID - идентификатор
     * SenderName - Игровое имя MC
     * SenderUUID - Идентификатор игрока MC
     * SenderDID - Идентификатор игрока в Discord
     * DateDispatch - Время и дата когда было отправлено
     * Type - Тип жалобы (Подробнее в команде wallet report)
     * Reason - Текст жалобы
     * From - Откуда поступила - MC | Discord
     * Inspector - Кто закрыт жалобу
     * ResponseType - Тип закрытия жалобы
     * ResponseText - Текст закрытия жалобы
     * DateResponse - Дата закрытия жалобы
     * Status - Переменная для проверки открыта или закрыта жалоба. [1 - открыта | 0 - закрыта]
     */

    private fun createTableReports() {
        val sql = """
            CREATE TABLE IF NOT EXISTS bank_reports (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                SenderName TEXT NOT NULL,
                SenderUUID TEXT NOT NULL,
                SenderDID TEXT NOT NULL,
                DateDispatch TEXT NOT NULL,
                Type TEXT NOT NULL,
                Reason TEXT NOT NULL,
                FromAddress TEXT NOT NULL, 
                Inspector TEXT,
                ResponseType TEXT,
                ResponseText TEXT,
                DateResponse TEXT,
                ReportID INTEGER NOT NULL,
                Status INTEGER NOT NULL
                );
        """.trimIndent()
        createTable(sql)
    }

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(url: String, plugin: App): DatabaseManager =
            instance ?: synchronized(this) {
                instance ?: DatabaseManager(url, plugin).also { instance = it }
            }
    }
}