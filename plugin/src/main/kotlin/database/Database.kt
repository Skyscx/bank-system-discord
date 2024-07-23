package database

import App
import discord.Functions
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException


class Database(dataFolder: File?, plugin: App?) {
    private var connection: Connection? = null
    private var plugin: App? = null
    val functions = Functions()

    init {
        this.plugin = plugin
        val datebaseFile = File(dataFolder, "database.db")
        if (!datebaseFile.exists()) {
            try {
                datebaseFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val url = "jdbc:sqlite:" + datebaseFile.absolutePath
        connection = DriverManager.getConnection(url)

        createTableAccounts()
    }
    /**Создание базы данных аккаунтов**/
    @Throws(SQLException::class)
    fun createTableAccounts() {
        val sql = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PlayerName TEXT NOT NULL," +
                "UUID TEXT NOT NULL," +
                "DiscordID INTEGER NOT NULL," +
                "DiscordName TEXT NOT NULL," +
                "DisplayDiscord TEXT NOT NULL," +
                "ActivatedBank INTEGER NOT NULL," +
                "Registration TEXT NOT NULL," +
                "`2f Auth` INTEGER NOT NULL," +   //NOT USAGE
                "PrivateKey TEXT NOT NULL," +    //NOT USAGE
                "Balance INTEGER NOT NULL," +
                "LastOperation TEXT NOT NULL," +  //NOT USAGE
                "Place TEXT NOT NULL," +   //NOT USAGE
                "Credits INTEGER NOT NULL," +  //NOT USAGE
                "USDT INTEGER NOT NULL," +  //NOT USAGE
                "Level INTEGER NOT NULL" +  //NOT USAGE
                ");"
        connection!!.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }
    /**Создание игрока в базе данных */
    fun insertPlayer(uuid: UUID): Player? {
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
                        if (discordID != null) {                    /**Сюда нужно сделать получение DSID**/
                            pstmt.setString(3, discordID)
                        }else{
                            pstmt.setString(3,null)
                        }
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
    /**Поиск игрока в базе данных**/
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

    fun getPlayerByUUID(uuid: UUID): Player? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        if (offlinePlayer.isOnline) {
            return offlinePlayer.player
        } else {
            return null
        }
    }

    fun closeConnection() {
        try {
            if (connection != null && !connection!!.isClosed) {
                connection!!.close()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
    /** Удаление игрока из базы данных - Last Update for Admin
     *
    fun deletePlayerLogyc(name: String, sender: CommandSender) {
    val future = checkPlayer(name)
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
    System.getLogger("Scheduler ENABLED")
    try {
    val result = future.join()
    if (!result) {
    System.getLogger("!result")
    sender.sendMessage("§3Такого игрока не существует в базе данных!")
    } else {
    sender.sendMessage("§3Игрок §7$name§3 удален из базы данных!")
    deletePlayer(name)
    System.getLogger("result")
    }
    } catch (e: CompletionException) {
    e.printStackTrace()
    }
    })
    }
     **/
}