package data

import App
import discord.FunctionsDiscord
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


class Database private constructor(url: String, plugin: App?) {
    private var connection: Connection? = null
    private var plugin: App? = null
    private val functionsDiscord = FunctionsDiscord()

    companion object {
        @Volatile
        private var instance: Database? = null
        fun getInstance(url: String, plugin: App?): Database =
            instance ?: synchronized(this) {
                instance ?: Database(url, plugin).also { instance = it }
            }
    }

    /**
     * Инициализация базы данных
     */

    init {
        this.plugin = plugin
        try {
            connection = DriverManager.getConnection(url)
            createTableUsers()
            createTableWallets()
            createTableHistory()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Создание таблицы пользователей
     */
    @Throws(SQLException::class)
    fun createTableUsers() {
        val sql = "CREATE TABLE IF NOT EXISTS bank_users (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PlayerName TEXT NOT NULL," +
                "UUID TEXT NOT NULL," +
                "DiscordID TEXT NOT NULL," +
                "ActivatedBank INTEGER NOT NULL," +
                "Registration TEXT NOT NULL," +
                "`2f Auth` INTEGER NOT NULL," +   //NOT USAGE
                "PrivateKey TEXT NOT NULL," +    //NOT USAGE
                "LastOperation TEXT NOT NULL," +  //NOT USAGE
                "USDT INTEGER NOT NULL," +  //NOT USAGE
                "Level INTEGER NOT NULL," +
                "DefaultWalletID INTEGER NOT NULL" +  //NOT USAGE
                ");"
        connection!!.createStatement().use { stmt ->
            stmt.executeUpdate(sql)
        }
    }

    /**
     * Создание таблицы кошельков
     */
    @Throws(SQLException::class)
    fun createTableWallets() {
        val sql = "CREATE TABLE IF NOT EXISTS bank_wallets (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "UUID TEXT NOT NULL," +
                "DiscordID TEXT NOT NULL," +
                "Registration TEXT NOT NULL," +
                "PrivateKey TEXT NOT NULL," +    //NOT USAGE
                "Balance INTEGER NOT NULL," + //TODO: ЗАМЕНИТЬ НА LONG либо реализовать по другому.
                "Currency TEXT NOT NULL," +
                "Name TEXT NOT NULL," +
                "Verification INTEGER NOT NULL," +
                "Deposit INTEGER NOT NULL," +
                "Inspector TEXT NOT NULL," +
                "VerificationDate TEXT NOT NULL," +
                "Status INTEGER NOT NULL" +
                ");"
        connection!!.createStatement().use { stmt ->
            stmt.executeUpdate(sql)
        }
    }

    /**
     * Создание таблицы истории банка
     */
    @Throws(SQLException::class)
    fun createTableHistory() {
        val sql = "CREATE TABLE IF NOT EXISTS bank_history (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "SenderIdAccount INTEGER NOT NULL," +
                "TargetIdAccount INTEGER NOT NULL," +
                "Amount INTEGER NOT NULL," +
                "Currency TEXT NOT NULL," +
                "SenderName TEXT NOT NULL," +
                "SenderUUID TEXT NOT NULL," +
                "SenderDiscordID TEXT NOT NULL," +
                "TargetName TEXT NOT NULL," +
                "TargetUUID TEXT NOT NULL," +
                "TargetDiscordID TEXT NOT NULL," +
                "Date TEXT NOT NULL," +
                "Status INTEGER NOT NULL" +
                ");"
        connection!!.createStatement().use { stmt ->
            stmt.executeUpdate(sql)
        }
    }

    /**
     * Полное удаление всех кошельков в таблице кошельков.
     */
    fun clearBankWalletsTable() {
        val sql = "DELETE FROM bank_wallets"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        createTableWallets()
    }

    /**
     * Создание пользователя в таблице пользователей
     */
    private fun insertPlayer(uuid: UUID) {
        val player = Bukkit.getOfflinePlayer(uuid)

        val playerName = player.name
        val playerUUID = player.uniqueId.toString()

        val discordID = functionsDiscord.getPlayerDiscordID(uuid)
        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql =
                    "INSERT INTO bank_users(" +
                            "PlayerName," +
                            "UUID," +
                            "DiscordID," +
                            "ActivatedBank," +
                            "Registration," +
                            "`2f Auth`," +
                            "PrivateKey," +
                            "LastOperation," +
                            "USDT," +
                            "Level," +
                            "DefaultWalletID" +
                            ") VALUES(?,?,?,?,?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, playerName) //Пользовательское игровое имя пользователя
                        pstmt.setString(2, playerUUID) //Пользовательский игровой UUID
                        if (discordID != null) { pstmt.setString(3, discordID) } else { pstmt.setString(3, null) } //Дискорд Айди привязанного аккаунта
                        pstmt.setBoolean(4, false) //Активирован ли банковский аккаунт
                        pstmt.setString(5, currentDate) //Время регистрации в банковской системе
                        pstmt.setBoolean(6, false) //Включено ли использование двухфакторной авторизации
                        pstmt.setString(7, "value") /**Приватный ключ пользователя - НЕОБХОДИМО РЕАЛИЗОВАТЬ**/
                        pstmt.setString(8, currentDate) /**Дата последней операции**/
                        pstmt.setInt(9, 0) /**Реальная валюта - USDT**/
                        pstmt.setInt(10, 0) /**Уровень**/
                        pstmt.setInt(11, 0) /**Номер кошелька по умолчанию**/
                        pstmt.executeUpdate()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }

    }

    /**
     * Создание кошелька в таблице кошельков
     */
    fun insertWallet(player: Player, currency: String, amount: Int, verificationInt: Int): CompletableFuture<Boolean> {
        val playerUUID = player.uniqueId
        val discordID = functionsDiscord.getPlayerDiscordID(playerUUID)
        val future = CompletableFuture<Boolean>()

        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql =
                    "INSERT INTO bank_wallets(" +
                            "UUID," +
                            "DiscordID," +
                            "Registration," +
                            "PrivateKey," +
                            "Balance," +
                            "Currency," +
                            "Name," +
                            "Verification," +
                            "Deposit," +
                            "Inspector," +
                            "VerificationDate," +
                            "Status" +
                            ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, playerUUID.toString())
                        pstmt.setString(2, discordID)
                        pstmt.setString(3, currentDate)
                        pstmt.setString(4, "value")
                        pstmt.setInt(5, amount)
                        pstmt.setString(6, currency)
                        pstmt.setString(7, "null")
                        pstmt.setInt(8, verificationInt)
                        pstmt.setInt(9, amount)
                        pstmt.setString(10, "null")
                        pstmt.setString(11, "null")
                        pstmt.setInt(12, 1)
                        val rowsAffected = pstmt.executeUpdate()
                        future.complete(rowsAffected > 0)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    future.complete(false)
                }
            })
        }

        return future
    }

    /**
     * Создание записи о проделанной операции в таблицу с историей.
     */
    private fun insertBankHistory(sender: Player, target: Player, senderWalletID: Int, targetWalletID:Int, amount: Int, currency: String, status: Int){
        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql =
                    "INSERT INTO bank_history(" +
                            "SenderIdAccount," +
                            "TargetIdAccount," +
                            "Amount," +
                            "Currency," +
                            "SenderName," +
                            "SenderUUID," +
                            "SenderDiscordID," +
                            "TargetName," +
                            "TargetUUID," +
                            "TargetDiscordID," +
                            "Date," +
                            "Status" +
                            ") VALUES(?,?,?,?,?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setInt(1, senderWalletID)
                        pstmt.setInt(2, targetWalletID)
                        pstmt.setInt(3, amount)
                        pstmt.setString(4, currency)
                        pstmt.setString(5, sender.name)
                        pstmt.setString(6, sender.uniqueId.toString())
                        pstmt.setString(7, getDiscordIDbyUUID(sender.uniqueId.toString()))
                        pstmt.setString(8, target.name)
                        pstmt.setString(9, target.uniqueId.toString())
                        pstmt.setString(10, getDiscordIDbyUUID(target.uniqueId.toString()))
                        pstmt.setString(11, currentDate)
                        pstmt.setInt(12, status)
                        pstmt.executeUpdate()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }
    }

    /**
     * Поиск пользователя в таблице пользователей по UUID (Необходим для создания пользователя в таблице пользователей)
     */
    fun checkPlayerTask(uuid: UUID) {
        val future = checkPlayer(uuid)
        Bukkit.getScheduler().runTaskAsynchronously(plugin!!, Runnable {
            try {
                val result = future.get()
                if (!result) {
                    insertPlayer(uuid)
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })
    }

    /**
     * Проверка на существование пользователя в таблице пользователей по UUID (Возвращение boolean)
     */
    private fun checkPlayer(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val sql = "SELECT * FROM bank_users WHERE UUID = ?"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, uuid.toString())
                        val rs = pstmt.executeQuery()
                        future.complete(rs.next())
                        rs.close()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    future.complete(false)
                }
            })
        }
        return future
    }

    /**
     * Получение UUID по DiscordID пользователя из таблицы пользователей
     */
    fun getUUIDbyDiscordID(id: String?): CompletableFuture<String?> {
        val future = CompletableFuture<String?>()
        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val sql = "SELECT UUID FROM bank_users WHERE DiscordID = ?"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, id)
                        val rs = pstmt.executeQuery()
                        if (rs.next()) {
                            future.complete(rs.getString("UUID"))
                        } else {
                            future.complete(null)
                        }
                        rs.close()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    future.completeExceptionally(e)
                }
            })
        }
        return future
    }

    /**
     * Получение UUID по ID WALLET из таблицы кошельков.
     */
    fun getPlayerByWalletID(walletID: Int): Player? {
        var uuid: String? = null
        val sql = "SELECT UUID FROM bank_wallets WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, walletID)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    uuid = resultSet.getString("UUID")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return uuid?.let { Bukkit.getPlayer(java.util.UUID.fromString(it)) }
    }
    /**
     * Получение баланса игрока из базы данных (NOW: Неактуальный метод, необходимо переделать)
     */
    fun getWalletBalance(playerUUID: String?): Int {
        val sql = "SELECT Balance FROM bank_users WHERE UUID = ?"
        var balance = 0

        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, playerUUID.toString())
                val result = pstmt.executeQuery()
                if (result.next()) {
                    balance = result.getInt("Balance")
                    println(balance)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return balance
    }

    /**
     * Обновление баланса игрока в базе данных (NOW: Неактуальный метод, необходимо переделать)
     */
    fun setWalletBalance(walletID: Int, balance: Int) : Boolean{
        val sql = "UPDATE bank_wallets SET Balance = ? WHERE ID = ? AND Status != 0"
        var boolean = false
        if (connection != null && !connection!!.isClosed) {
            try {
                connection!!.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, balance)
                    pstmt.setInt(2, walletID)
                    val rowsAffected = pstmt.executeUpdate()
                    if (rowsAffected > 0 ) boolean = true

                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        return boolean
    }

    /**
     * Обновление баланса кошелька по ID кошелька. (NEW)
     */
    private fun updateWalletBalance(accountId: Int, amount: Int) {
        val sql = "UPDATE bank_wallets SET balance = balance + ? WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, amount)
                pstmt.setInt(2, accountId)
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Метод перевода со счета на счет.
     */
    fun transferFunds(
        sender: Player,
        target: Player,
        senderWalletID: Int,
        targetWalletID: Int,
        amount: Int,
        currency: String,
        status: Int
    ): Boolean {
        val senderBalanceSql = "SELECT balance FROM bank_wallets WHERE ID = ?"
        var senderBalance: Int? = null
        try {
            connection?.prepareStatement(senderBalanceSql)?.use { pstmt ->
                pstmt.setInt(1, senderWalletID)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    senderBalance = resultSet.getInt("balance")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        if (senderBalance == null || senderBalance!! < amount) {
            println("Недостаточно средств на счете отправителя")
            return false
        }

        // Обновление баланса отправителя и получателя
        updateWalletBalance(senderWalletID, -amount)
        updateWalletBalance(targetWalletID, amount)

        // Вставка записи в таблицу bank_history
        insertBankHistory(
            sender,
            target,
            senderWalletID,
            targetWalletID,
            amount,
            currency,
            status
        )
        return true
    }
    /**
     * Счетчик количества кошельков пользователя по UUID в таблице кошельков
     */
    fun getWalletsCount(uuid: String?): Int {
        val sql = "SELECT * FROM bank_wallets WHERE UUID = ? AND Status != 0"
        var count = 0

        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid.toString())
                val result = pstmt.executeQuery()
                while (result.next()) {
                    count++
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return count
    }

    /**
     * Присвоение уникального имени кошелька пользователя по ID кошелька.
     *
     * TODO: Необходимо сделать проверку на занятость имени кошелька.
     */
    fun setWalletName(uuid: String?, name: String, id: String) {
        val sql = "UPDATE bank_wallets SET Name = ? WHERE UUID = ? AND ID = ?"
        if (connection != null && !connection!!.isClosed) {
            try {
                connection!!.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, name)
                    pstmt.setString(2, uuid)
                    pstmt.setString(3, id)
                    pstmt.executeUpdate()
                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Получение ID кошелька по имени кошелька.
     */
    fun getIDByWalletName(name: String): Int? {
        var id: Int? = null

        val sql = "SELECT ID FROM bank_wallets WHERE Name = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, name)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    id = resultSet.getInt("ID")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return id
    }
    fun getWalletID(identifier: String): Int? {
        return identifier.toIntOrNull() ?: getIDByWalletName(identifier)
    }

    /**
     * Генерация списка из ID кошельков в размере 5 записей, поиск осуществляется по таблице кошельков по статусу верификации.
     *
     * Необходим для визуального отображения о процессе верификации кошельков
     */
    fun getUnverifiedWallets(): List<String> {
        val unverifiedAccounts = mutableListOf<String>()

        val sql = "SELECT * FROM bank_wallets WHERE Verification = 0 ORDER BY id ASC LIMIT 5"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                val result = pstmt.executeQuery()
                while (result.next()) {
                    val id = result.getInt("id")
                    unverifiedAccounts.add(id.toString())
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return unverifiedAccounts
    }

    /**
     * Генерация строки с необходимыми данными о статусе верификации кошелька по ID кошельку из таблицы кошельков.
     */
    fun getPlayerDataByID(id: Int): String? {
        var playerData: String? = null

        val sql = "SELECT UUID FROM bank_wallets WHERE id = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                val result = pstmt.executeQuery()
                if (result.next()) {
                    val uuid = result.getString("UUID")
                    val playerDataSql = "SELECT PlayerName, DiscordID, Registration, `2f Auth`, USDT, Level FROM bank_users WHERE UUID = ?"
                    try {
                        connection?.prepareStatement(playerDataSql)?.use { pstmt2 ->
                            pstmt2.setString(1, uuid)
                            val result2 = pstmt2.executeQuery()
                            if (result2.next()) {
                                val playerName = result2.getString("PlayerName")
                                val discordId = result2.getString("DiscordID")
                                val registration = result2.getString("Registration")
                                val usdt = result2.getInt("USDT")
                                val level = result2.getInt("Level")
                                playerData = "id: $id $playerName : $discordId : $registration : $usdt : $level"
                            }
                        }
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return playerData
    }

    /**
     * Получение статуса верификации кошелька в таблице кошельков
     *
     * 1 - одобрен || 0 - ожидание || -1 - отказан
     */
    fun getVerificationWallet(id: Int): Int {
        var verification = 0

        val sql = "SELECT Verification FROM bank_wallets WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    verification = resultSet.getInt("Verification")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return verification
    }

    /**
     * Присвоение статуса верификации кошелька в таблице кошельков
     *
     * 1 - одобрен || 0 - ожидание || -1 - отказан
     */
    fun setVerificationWallet(id: Int, verification: Int): Boolean {
        var result = false

        val sql = "UPDATE bank_wallets SET Verification = ? WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, verification)
                pstmt.setInt(2, id)
                pstmt.executeUpdate()
                result = true
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return result
    }

    /**
     * Присвоение записи даты верификации в формате dd:MM:yyyy HH:mm:ss по ID кошельку в таблице кошельков.
     */
    fun setVerificationWalletDate(id: Int) {
        val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
        val sql = "UPDATE bank_wallets SET VerificationDate = ? WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, currentDate)
                pstmt.setInt(2, id)
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Получение даты верификации в формате dd:MM:yyyy HH:mm:ss по ID кошельку в таблице кошельков.
     */
    fun getVerificationWalletDate(id: Int) : String{
        val sql = "SELECT VerificationDate FROM bank_wallets WHERE ID = ?"
        var date = "Нету"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, id)
            pstmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    date = resultSet.getString("VerificationDate")
                }
            }
        }
        return date
    }

    /**
     * Присвоение записи о проверяющей в виде DiscordID в таблице кошельков по ID.
     */
    fun setInspectorWallet(id: Int, inspector: String) {
        val sql = "UPDATE bank_wallets SET Inspector = ? WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, inspector)
                pstmt.setInt(2, id)
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Получение DiscordID проверяющего по ID в таблице кошельков.
     */
    fun getInspectorWallet(id: Int): String? {
        var inspectorID: String? = null
        val sql = "SELECT Inspector FROM bank_wallets WHERE ID = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setInt(1, id)
            pstmt.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    inspectorID = resultSet.getString("Inspector")
                }
            }
        }
        return inspectorID
    }

    /**
     * Проверка по ID есть ли у пользователя доступный депозит для вывода (Возвращение boolean)
     */
    fun isDepositWalletAvailable(id: Int): Boolean {
        var deposit: String? = null
        val verification = getVerificationWallet(id)

        if (verification == -1) {
            val sql = "SELECT Deposit FROM bank_wallets WHERE ID = ?"
            try {
                connection?.prepareStatement(sql)?.use { pstmt ->
                    pstmt.setInt(1, id)
                    val resultSet = pstmt.executeQuery()
                    if (resultSet.next()) {
                        deposit = resultSet.getString("Deposit")
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        return deposit != null
    }

    /**
     * Установление значения депозита в таблице кошельков по ID кошелька.
     */
    fun setDepositWallet(id: Int, deposit: String) {
        val sql = "UPDATE bank_wallets SET Deposit = ? WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, deposit)
                pstmt.setInt(2, id)
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Получение значение депозита по ID кошельку из таблицы кошельков.
     */
    fun getDepositWallet(id: Int): Int? {
        var deposit: Int? = null

        val sql = "SELECT Deposit FROM bank_wallets WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    deposit = resultSet.getInt("Deposit")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return deposit
    }

    /**
     * Получение списка с ID кошельками, которые не одобрили, из таблицы кошельков.
     */
    fun getIdsWalletsReturnDepositByUUID(uuid: String): List<Int> {
        val depositIds = mutableListOf<Int>()

        val sql = "SELECT id FROM bank_wallets WHERE UUID = ? AND Verification = -1"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid)
                val resultSet = pstmt.executeQuery()
                while (resultSet.next()) {
                    val id = resultSet.getInt("ID")
                    depositIds.add(id)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return depositIds
    }
    /**
     * Получение списка с ID кошельками, которые существуют у пользователя с идентичным UUID
     */
    fun getIdsWalletsOwnerByUUID(uuid: String): List<Int> {
        val depositIds = mutableListOf<Int>()

        val sql = "SELECT ID FROM bank_wallets WHERE UUID = ? AND Status != 0"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid)
                val resultSet = pstmt.executeQuery()
                while (resultSet.next()) {
                    val id = resultSet.getInt("ID")
                    depositIds.add(id)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return depositIds
    }

    /**
     * Сохранение номера кошелька как основного кошелька для транзакций.
     */
    fun setDefaultWalletID(uuid: String?, walletID: Int) {
        val sql = "UPDATE bank_users SET DefaultWalletID = ? WHERE UUID = ?"
        if (connection != null && !connection!!.isClosed) {
            try {
                connection!!.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, walletID)
                    pstmt.setString(2, uuid.toString())
                    pstmt.executeUpdate()
                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Получение основного кошелька для транзакций по UUID
     */
    fun getDefaultWalletIDByUUID(uuid: String): Int? {
        var defaultWalletID: Int? = null
        val sql = "SELECT DefaultWalletID FROM bank_users WHERE UUID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    defaultWalletID = resultSet.getInt("DefaultWalletID")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return defaultWalletID
    }

    /**
     * Получение валюты кошелька по Wallet ID.
     */
    fun getWalletCurrency(walletID: Int): String? {
        var currency: String? = null
        val sql = "SELECT Currency FROM bank_wallets WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, walletID)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    currency = resultSet.getString("Currency")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return currency
    }
    /**
     * Удаление кошелька из таблицы кошельков по ID кошельку.
     */
    fun deleteUserWallet(id: Int): Boolean {
        var boolean = false
        val sql = "UPDATE bank_wallets SET Name = 'NULL', Status = 0 WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                val rowsAffected = pstmt.executeUpdate()
                if (rowsAffected > 0) boolean = true
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            // Возвращаем false в случае ошибки
        }
        return boolean
    }

    /**
     * Получение последнего не использованного ID кошелька.
     */
    fun getLastIDWalletFree(): Int? {
        var lastId: Int? = null
        val sql = "SELECT MAX(ID) FROM bank_wallets"

        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    lastId = resultSet.getInt(1) + 1
                }
                resultSet.close()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return lastId
    }

    /**
     * Проверка существует ли данный ID кошелек в таблице кошельков.
     */
    fun doesIdExistWallet(id: Int): Boolean {
        var exists = false
        val sql = "SELECT 1 FROM bank_wallets WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                val resultSet = pstmt.executeQuery()
                exists = resultSet.next()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return exists
    }

    /**
     * Получение DiscordID по UUID пользователя по таблице пользователей.
     */
    fun getDiscordIDbyUUID(uuid: String?): String? {
        val sql = "SELECT DiscordID FROM bank_users WHERE UUID = ?"
        var discordID: String? = null

        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid.toString())
                pstmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        discordID = rs.getString("DiscordID")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return discordID
    }

    /**
     * Получение UUID пользователя по ID пользователю из таблицы кошельков.
     */
    fun getUUID(id: Int): String? {
        var uuid: String? = null

        val sql = "SELECT UUID FROM bank_wallets WHERE ID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    uuid = resultSet.getString("UUID")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return uuid
    }
    /**
     * Проверка существования UUID в таблице кошельков
     */
    fun doesUUIDWalletsExist(uuid: String): Boolean {
        var boolean = false
        val sql = "SELECT COUNT(*) FROM bank_wallets WHERE UUID = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid)
                val resultSet = pstmt.executeQuery()
                if (resultSet.next()) {
                    val count = resultSet.getInt(1)
                    if (count > 0) boolean = true
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return boolean
    }

    /**
     * Закрытие соединения
     */
    fun closeConnection() {
        try {
            if (connection != null && !connection!!.isClosed) {
                connection!!.close()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}