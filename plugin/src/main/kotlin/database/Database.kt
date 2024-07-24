package database

import App
import discord.Functions
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


class Database(url: String, plugin: App?) {
    private var connection: Connection? = null
    private var plugin: App? = null
    //private val dateFormat = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
    val functions = Functions()
    init {
        this.plugin = plugin
        try {
            connection = DriverManager.getConnection(url)
            createTableUsers()
            createTableAccounts()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Создание базы данных аккаунтов
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
                "Level INTEGER NOT NULL" +  //NOT USAGE
                ");"
        connection!!.createStatement().use { stmt ->
            stmt.executeUpdate(sql)
        }
    }
    /**
     * Создание базы данных счетов
     */
    @Throws(SQLException::class)
    fun createTableAccounts() {
        val sql = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "UUID TEXT NOT NULL," +
                "DiscordID TEXT NOT NULL," +
                "Registration TEXT NOT NULL," +
                "PrivateKey TEXT NOT NULL," +    //NOT USAGE                                                              //TODO: Подумать о его реализации
                "Balance INTEGER NOT NULL," + //TODO: ЗАМЕНИТЬ НА LONG либо реализовать по другому.
                "Currency TEXT NOT NULL," +
                "Name TEXT NOT NULL" +
                ");"
        connection!!.createStatement().use { stmt ->
            stmt.executeUpdate(sql)
        }
    }
    /**
     * Создание игрока в базе данных
     */
    private fun insertPlayer(uuid: UUID): Player? {
        val player = getPlayerByUUID(uuid) ?: return null
        val playerName = player.name
        val playerUUID = player.uniqueId.toString()
        val discordID = functions.getPlayerDiscordID(uuid)
        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql =
                    "INSERT INTO bank_users(PlayerName,UUID,DiscordID,ActivatedBank,Registration,`2f Auth`,PrivateKey,LastOperation,USDT,Level) VALUES(?,?,?,?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, playerName) //Пользовательское игровое имя пользователя
                        pstmt.setString(2, playerUUID) //Пользовательский игровой UUID
                        if (discordID != null) { pstmt.setString(3, discordID) }else{ pstmt.setString(3,null) } //Дискорд Айди привязанного аккаунта
                        //pstmt.setInt(4, 0) /**Сюда нужно сделать получение UsernameDiscord**/
                        //pstmt.setInt(5, 0) /**Сюда нужно сделать получение Отображающегося имени дискорд пользователя**/
                        pstmt.setBoolean(4, false) //Активирован ли банковский аккаунт
                        pstmt.setString(5, currentDate) //Время регистрации в банковской системе
                        pstmt.setBoolean(6, false) //Включено ли использование двухфакторной авторизации
                        pstmt.setString(7, "value") /**Приватный ключ пользоваться - НЕОБХОДИМО РЕАЛИЗОВАТЬ**/
                        //pstmt.setInt(10, 0) //Игровой баланс игрока
                        pstmt.setString(8, currentDate) /**Дата последней операции**/
                        //pstmt.setInt(12, 0) /**Место в топе**/
                        //pstmt.setInt(13, 0) /**Кредиты/Займы**/
                        pstmt.setInt(9, 0) /**Реальная валюта - USDT**/
                        pstmt.setInt(10, 0) /**Уровень**/
                        pstmt.executeUpdate()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }
        return player
    }
    /**
     *Создание счета в базе данных
     */
    fun insertAccount(player: Player, currency: String){
        val playerUUID = player.uniqueId
        val discordID = functions.getPlayerDiscordID(playerUUID)
        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
                val sql =
                    "INSERT INTO bank_accounts(UUID,DiscordID,Registration,PrivateKey,Balance,Currency,Name) VALUES(?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, playerUUID.toString())
                        pstmt.setString(2, discordID)
                        pstmt.setString(3, currentDate)
                        pstmt.setString(4, "value")
                        pstmt.setInt(5, 0)
                        pstmt.setString(6, currency)
                        pstmt.setString(7, "null")
                        pstmt.executeUpdate()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            })
        }
    }
    /**
     * Поиск игрока в базе данных
     */
    fun checkPlayerTask(uuid: UUID) {
        val future = checkPlayer(uuid)
        Bukkit.getScheduler().runTaskAsynchronously(plugin!!, Runnable {
            try {
                val result = future.get() // ждем завершения асинхронной задачи
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
    fun checkPlayer(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val sql = "SELECT * FROM bank_users WHERE UUID = ?"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, uuid.toString())
                        val rs = pstmt.executeQuery()
                        future.complete(rs.next())
                        rs.close() // не забываем закрыть ResultSet
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
     * Получение UUID по DiscordID пользователя
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
     * Получение игрока по UUID
     */
    fun getPlayerByUUID(uuid: UUID): Player? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        return offlinePlayer.player
    }
    /**
     * Получение баланса игрока из базы данных
     */
    fun getPlayerBalance(playerUUID: String?): Int {
        val sql = "SELECT Balance FROM bank_users WHERE UUID = ?"
        var balance = 0

        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, playerUUID.toString())
                val result = pstmt.executeQuery()
                if (result.next()){
                    balance = result.getInt("Balance")
                    println(balance)
                }
            }
        } catch (e: SQLException){
            e.printStackTrace()
        }

        return balance
    }
    /**
     * Обновление баланса игрока в базе данных
     */
    fun setPlayerBalance(playerUUID: String?, balance: Int) {
        val sql = "UPDATE bank_users SET Balance = ? WHERE UUID = ?"
        if (connection != null && !connection!!.isClosed) {
            try {
                connection!!.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, balance)
                    pstmt.setString(2, playerUUID.toString())
                    pstmt.executeUpdate()
                }

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
    /**
     * Счетчик счетов игрока по UUID
     */
    fun getAccountCount(uuid: String?): Int {
        val sql = "SELECT * FROM bank_accounts WHERE UUID = ?"
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
    fun setAccountName(uuid: String?, name: String, id: String){
        val sql = "UPDATE bank_accounts SET Name = ? WHERE UUID = ? AND id = ?"
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