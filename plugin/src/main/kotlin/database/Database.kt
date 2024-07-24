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
            createTableAccounts()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Создание базы данных аккаунтов
     */
    @Throws(SQLException::class)
    fun createTableAccounts() {
        val sql = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PlayerName TEXT NOT NULL," +
                "UUID TEXT NOT NULL," +
                "DiscordID TEXT NOT NULL," +
                "DiscordName TEXT NOT NULL," +
                "DisplayDiscord TEXT NOT NULL," +
                "ActivatedBank INTEGER NOT NULL," +
                "Registration TEXT NOT NULL," +
                "`2f Auth` INTEGER NOT NULL," +   //NOT USAGE
                "PrivateKey TEXT NOT NULL," +    //NOT USAGE
                "Balance INTEGER NOT NULL," + //TODO: ЗАМЕНИТЬ НА LONG либо реализовать по другому.                         //TODO:Перенести в другую БД
                "LastOperation TEXT NOT NULL," +  //NOT USAGE
                "Place TEXT NOT NULL," +   //NOT USAGE                                                                      //TODO:Перенести в другую БД
                "Credits INTEGER NOT NULL," +  //NOT USAGE                                                                  //TODO:Перенести в другую БД
                "USDT INTEGER NOT NULL," +  //NOT USAGE                                                                     //TODO:Перенести в другую БД
                "Level INTEGER NOT NULL" +  //NOT USAGE
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
                    "INSERT INTO bank_accounts(PlayerName,UUID,DiscordID,DiscordName,DisplayDiscord,ActivatedBank,Registration,`2f Auth`,PrivateKey,Balance,LastOperation,Place,Credits,USDT,Level) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, playerName) //Пользовательское игровое имя пользователя
                        pstmt.setString(2, playerUUID) //Пользовательский игровой UUID
                        if (discordID != null) { pstmt.setString(3, discordID) }else{ pstmt.setString(3,null) } //Дискорд Айди привязанного аккаунта
                        pstmt.setInt(4, 0) /**Сюда нужно сделать получение UsernameDiscord**/
                        pstmt.setInt(5, 0) /**Сюда нужно сделать получение Отображающегося имени дискорд пользователя**/
                        pstmt.setBoolean(6, false) //Активирован ли банковский аккаунт
                        pstmt.setString(7, currentDate) //Время регистрации в банковской системе
                        pstmt.setBoolean(8, false) //Включено ли использование двухфакторной авторизации
                        pstmt.setString(9, "value") /**Приватный ключ пользоваться - НЕОБХОДИМО РЕАЛИЗОВАТЬ**/
                        pstmt.setInt(10, 0) //Игровой баланс игрока
                        pstmt.setString(11, currentDate) /**Дата последней операции**/
                        pstmt.setInt(12, 0) /**Место в топе**/
                        pstmt.setInt(13, 0) /**Кредиты/Займы**/
                        pstmt.setInt(14, 0) /**Реальная валюта - USDT**/
                        pstmt.setInt(15, 0) /**Уровень**/
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
                val sql = "SELECT * FROM bank_accounts WHERE UUID = ?"
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
                val sql = "SELECT UUID FROM bank_accounts WHERE DiscordID = ?"
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
        val sql = "SELECT Balance FROM bank_accounts WHERE UUID = ?"
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
        val sql = "UPDATE bank_accounts SET Balance = ? WHERE UUID = ?"
        if (connection != null && !connection!!.isClosed) {
            try {
                connection!!.prepareStatement(sql).use { pstmt ->
                    pstmt.setInt(1, balance)
                    pstmt.setString(2, playerUUID.toString())
                    pstmt.executeUpdate()

                    println(pstmt)
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