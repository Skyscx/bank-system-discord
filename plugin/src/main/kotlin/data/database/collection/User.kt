package data.database.collection

import App
import data.database.DatabaseManager
import discord.FunctionsDiscord
import org.bukkit.Bukkit
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class User(private var dbManager: DatabaseManager, private var functionsDiscord: FunctionsDiscord, private var plugin: App) {

    /**
     * Создание пользователя в таблице пользователей
     */
    private fun insertPlayer(uuid: UUID): Boolean {
        val player = Bukkit.getOfflinePlayer(uuid)
        val playerName = player.name as String
        val playerUUID = player.uniqueId.toString()
        val discordID = functionsDiscord.getPlayerDiscordID(uuid)         //TODO: Добавить проверку на null discordID
        val privateKey = "Admin: create function"
        val usdt = 1
        val level = 0
        val defaultWallet = 0
        val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
        val sql = """
                    INSERT INTO bank_users(
                        PlayerName, UUID, DiscordID, ActivatedBank, Registration,
                        `2f Auth`, PrivateKey, LastOperation, USDT, Level, DefaultWalletID
                    ) VALUES(?,?,?,?,?,?,?,?,?,?,?)
                """.trimIndent()
        return dbManager.executeUpdate(sql, playerName, playerUUID, discordID!!, false, currentDate, false, privateKey, currentDate, usdt, level, defaultWallet)

    }

    /**
     * Проверка на существование пользователя в таблице пользователей по UUID (Возвращение boolean)
     */
    private fun checkPlayer(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val sql = "SELECT * FROM bank_users WHERE UUID = ?"

            try {
                val result = dbManager.executeQuery(sql, uuid.toString())
                future.complete(result.isNotEmpty())
            } catch (e: SQLException) {
                e.printStackTrace()
                future.complete(false)
            }
        })

        return future
    }

    /**
     * Поиск пользователя в таблице пользователей по UUID (Необходим для создания пользователя в таблице пользователей)
     */
    fun checkPlayerTaskInsert(uuid: UUID) {
        val future = checkPlayer(uuid)

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
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
     * Получение DiscordID по UUID пользователя по таблице пользователей.
     */
    fun getDiscordIDbyUUID(uuid: String): String? {
        val sql = "SELECT DiscordID FROM bank_users WHERE UUID = ?"
        val result = dbManager.executeQuery(sql, uuid)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("DiscordID") as? String
    }

    /**
     * Получение DefaultWalletID по UUID пользователя по таблице пользователей.
     */
    fun getDefaultWalletByUUID(uuid: String): Int? {
        val sql = "SELECT DefaultWalletID FROM bank_users WHERE UUID = ?"
        val result = dbManager.executeQuery(sql, uuid)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("DefaultWalletID") as? Int
    }

    fun setDefaultWalletByUUID(uuid: String, id: Int): Boolean {
        val sql = "UPDATE bank_users SET DefaultWalletID = ? WHERE UUID = ?"
        return dbManager.executeUpdate(sql, id, uuid)
    }


    /**
     * Получение PlayerName по UUID пользователя по таблице пользователей.
     */
    fun getPlayerNameByUUID(uuid: String): String? {
        val sql = "SELECT PlayerName FROM bank_users WHERE UUID = ?"
        val result = dbManager.executeQuery(sql, uuid)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("PlayerName") as? String
    }



    /**
     * Получение UUID по DiscordID пользователя из таблицы пользователей
     */
    fun getUUIDbyDiscordID(id: String): CompletableFuture<String?> {
        val future = CompletableFuture<String?>()

        plugin.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val sql = "SELECT UUID FROM bank_users WHERE DiscordID = ?"

                try {
                    val result = dbManager.executeQuery(sql, id)
                    if (result.isNotEmpty()) {
                        val row = result.firstOrNull()
                        future.complete(row?.get("UUID") as? String)
                    } else {
                        future.complete(null)
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
     * Получение пользовательского UUID по имени пользователя по базе данных
     */
    fun getUUIDbyPlayerName(playerName: String): String? {
        val sql = "SELECT UUID FROM bank_users WHERE PlayerName = ?"
        var uuid: String? = null

        val result = dbManager.executeQuery(sql, playerName)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            uuid = row?.get("UUID") as? String
        }

        return uuid
    }

    /**
     * Получение ID USER по UUID пользователя по таблице пользователей.
     */
    fun getIdUserByUUID(uuid: String): Int? {
        val sql = "SELECT ID FROM bank_users WHERE UUID = ?"
        val result = dbManager.executeQuery(sql, uuid)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("ID") as? Int
    }



    companion object {
        @Volatile
        private var instance: User? = null

        fun getInstance(dbManager: DatabaseManager, plugin: App, functionsDiscord: FunctionsDiscord): User =
            instance ?: synchronized(this) {
                instance ?: User(dbManager, functionsDiscord, plugin).also { instance = it }
            }
    }

}
