package database

import App
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException


class Database(dataFolder: File?, plugin: App?) {
    private var connection: Connection? = null
    private var plugin: App? = null

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
                "Registration TIMESTAMP NOT NULL," +
                "`2f Auth` INTEGER NOT NULL," +   //NOT USAGE
                "PrivateKey TEXT NOT NULL," +    //NOT USAGE
                "Balance INTEGER NOT NULL," +
                "LastOperation TIMESTAMP NOT NULL," +  //NOT USAGE
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
        plugin?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val sql =
                    "INSERT INTO bank_accounts(PlayerName,UUID,DiscordID,DiscordName,DisplayDiscord,ActivatedBank,Registration,`2f Auth`,PrivateKey,Balance,LastOperation,Place,Credits,USDT,Level) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                try {
                    connection?.prepareStatement(sql)?.use { pstmt ->
                        pstmt.setString(1, playerName) //Пользовательское игровое имя пользователя
                        pstmt.setString(2, playerUUID) //Пользовательский игровой UUID
                        pstmt.setInt(3, 0) /**Сюда нужно сделать получение DSID**/
                        pstmt.setInt(4, 0) /**Сюда нужно сделать получение UsernameDiscord**/
                        pstmt.setInt(5, 0) /**Сюда нужно сделать получение Отображающегося имени дискорд пользователя**/
                        pstmt.setBoolean(6, false) //Активирован ли банковский аккаунт
                        pstmt.setTimestamp(7, Timestamp(System.currentTimeMillis())) //Время регистрации в банковской системе
                        pstmt.setBoolean(8, false) //Включено ли использование двухфакторной авторизации
                        pstmt.setString(9, "value") /**Приватный ключ пользоваться - НЕОБХОДИМО РЕАЛИЗОВАТЬ**/
                        pstmt.setInt(10, 0) //Игровой баланс игрока
                        pstmt.setTimestamp(11, Timestamp(System.currentTimeMillis())) /**Дата последней операции**/
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
                val result = future.join()
                if (!result) {
                    insertPlayer(uuid)
                }
            } catch (e: CompletionException) {
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
                        pstmt.executeQuery().use { rs ->
                            future.complete(rs.next())
                        }
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