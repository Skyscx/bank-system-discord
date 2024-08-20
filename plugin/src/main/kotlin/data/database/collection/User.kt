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

    fun getDiscordIDbyUUID(uuid: String): String? {
        val sql = "SELECT DiscordID FROM bank_users WHERE UUID = ?"
        val result = dbManager.executeQuery(sql, uuid)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("DiscordID") as? String
    }

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

}
