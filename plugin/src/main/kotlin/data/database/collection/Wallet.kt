package data.database.collection

import App
import data.database.DatabaseManager
import discord.FunctionsDiscord
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture

class Wallet(private val dbManager: DatabaseManager, private val plugin: App, private var functionsDiscord: FunctionsDiscord) {

    private val historyDB = History(dbManager, functionsDiscord, plugin)

    fun insertWallet(player: Player, currency: String, amount: Int, verificationInt: Int): CompletableFuture<Boolean> {
        val playerUUID = player.uniqueId
        val discordID = functionsDiscord.getPlayerDiscordID(playerUUID)
        val privateKey = "Admin: create function"
        val balance = 0
        val name = "Admin: create function" // Создать присвоение имени
        val inspector = "Null"
        val dateVerification = "Admin: create function"

        val future = CompletableFuture<Boolean>()
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
            val sql = """
            INSERT INTO bank_wallets(
                UUID, DiscordID, Registration, PrivateKey, Balance, Currency,
                Name, Verification, Deposit, Inspector, VerificationDate, Status
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()

            try {
                val result = dbManager.executeUpdate(
                    sql,
                    playerUUID.toString(), discordID!!, currentDate, privateKey, balance, currency, name,
                    verificationInt, amount, inspector, dateVerification, 1
                )
                future.complete(result)
            } catch (e: SQLException) {
                e.printStackTrace()
                future.complete(false)
            }
        })

        return future
    }

    fun getVerificationWallet(id: Int): Int {
        var verification = 0
        val sql = "SELECT Verification FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        result.forEach { row ->
            verification = row["Verification"] as Int
        }
        return verification
    }

    fun setVerificationWallet(id: Int, verification: Int): Boolean {
        val sql = "UPDATE bank_wallets SET Verification = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, verification, id)
    }

    fun getInspectorWallet(id: Int): String? {
        val sql = "SELECT Inspector FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) {
            plugin.logger.warning("No Inspector found for wallet ID: $id")
            return null
        }
        val row = result.firstOrNull()
        return row?.get("Inspector") as? String
    }

    fun setInspectorWallet(id: Int, inspector: String): Boolean {
        val sql = "UPDATE bank_wallets SET Inspector = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, inspector, id)
    }

    fun getVerificationWalletDate(id: Int): String? {
        val sql = "SELECT VerificationDate FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) {
            plugin.logger.warning("No VerificationDate found for wallet ID: $id")
            return null
        }
        val row = result.firstOrNull()
        return row?.get("VerificationDate") as? String
    }

    fun setVerificationWalletDate(id: Int): Boolean {
        val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
        val sql = "UPDATE bank_wallets SET VerificationDate = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, currentDate, id)
    }

    fun setDepositWallet(id: Int, deposit: String): Boolean {
        val sql = "UPDATE bank_wallets SET Deposit = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, deposit, id)
    }

    fun getUUIDbyWalletID(id: Int): String? {
        val sql = "SELECT UUID FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) {
            plugin.logger.warning("No UUID found for wallet ID: $id")
            return null
        }
        val row = result.firstOrNull()
        return row?.get("UUID") as? String
    }

    fun getPlayerByWalletID(id: Int): Player? {
        val sql = "SELECT UUID FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) {
            plugin.logger.warning("No Player found for wallet ID: $id")
            return null
        }
        val row = result.firstOrNull()
        val uuid = row?.get("UUID") as? String
        return uuid?.let { Bukkit.getPlayer(UUID.fromString(it)) }
    }

    fun getWalletBalance(id: Int): Int {
        val sql = "SELECT Balance FROM bank_wallets WHERE ID = ?"
        var balance = 0

        try {
            val result = dbManager.executeQuery(sql, id)
            if (result.isNotEmpty()) {
                val row = result.firstOrNull()
                balance = row?.get("Balance") as? Int ?: 0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return balance
    }

// NOT ACTUALLY
//    fun setWalletBalance(id: Int, balance: Int): Boolean {
//        val sql = "UPDATE bank_wallets SET Balance = ? WHERE ID = ? AND Status != 0"
//        return dbManager.executeUpdate(sql, balance, id)
//    }

    fun updateWalletBalance(id: Int, amount: Int): Boolean {
        val sql = "UPDATE bank_wallets SET balance = balance + ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, amount, id)
    }

    fun checkWalletStatus(walletID: Int): Boolean {
        val sql = "SELECT Status FROM bank_wallets WHERE ID = ?"
        var status = false

        try {
            val result = dbManager.executeQuery(sql, walletID)
            if (result.isNotEmpty()) {
                val row = result.firstOrNull()
                status = (row?.get("Status") as? Int) == 1
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return status
    }

    fun transferCash(
        sender: Player,
        target: Player,
        senderWalletID: Int,
        targetWalletID: Int,
        amount: Int,
        currency: String,
        status: Int
    ): Boolean {
        //todo: подумать над проверками
        if (!checkWalletStatus(senderWalletID)) {
            //message sender - wallet not access
            return false
        }
        if (!checkWalletStatus(targetWalletID)) {
            //message target - wallet not access
            return false
        }
        val sql = "SELECT Balance FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, senderWalletID)
        if (result.isEmpty()) {
            plugin.logger.warning("No Balance found for wallet ID: $senderWalletID")
            return false
        }
        val row = result.firstOrNull()
        val senderBalance = row?.get("Balance") as? Int
        if (senderBalance == null || senderBalance < amount) { //TODO: Проверку на null senderBalance сделать отдельной
            println("Недостаточно средств на счете отправителя")
            return false
        }
        updateWalletBalance(senderWalletID, -amount)
        updateWalletBalance(targetWalletID, amount)

        historyDB.insertBankHistory(sender, target, senderWalletID, targetWalletID, amount, currency, status)
        return true
    }

    fun getWalletsCount(uuid: String): Int {
        val sql = "SELECT COUNT(*) AS count FROM bank_wallets WHERE UUID = ? AND Status != 0"
        var count = 0

        try {
            val result = dbManager.executeQuery(sql, uuid)
            if (result.isNotEmpty()) {
                val row = result.firstOrNull()
                count = row?.get("count") as? Int ?: 0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return count
    }

    fun setWalletName(uuid: String, name: String, id: Int): Boolean {
        val sql = "UPDATE bank_wallets SET Name = ? WHERE UUID = ? AND ID = ?"
        return dbManager.executeUpdate(sql, name, uuid, id)
    }

    fun getIDByWalletName(name: String): Int? {
        val sql = "SELECT ID FROM bank_wallets WHERE Name = ?"
        val result = dbManager.executeQuery(sql, name)
        val row = result.firstOrNull()
        val id = row?.get("ID") as? Int
        return id
    }

    fun getWalletID(identifier: String): Int? {
        return identifier.toIntOrNull() ?: getIDByWalletName(identifier)
    }

    fun getUnverifiedWallets(): List<String> {
        val unverifiedAccounts = mutableListOf<String>()
        val sql = "SELECT * FROM bank_wallets WHERE Verification = 0 AND Status != 0 ORDER BY id ASC LIMIT 5"

        try {
            val result = dbManager.executeQuery(sql)
            for (row in result) {
                val id = row["id"] as Int
                unverifiedAccounts.add(id.toString())
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return unverifiedAccounts
    }
    fun getPlayerDataByID(id: Int): String? { //TODO: РАЗЪЕДЕНИТЬ ОБРАЩЕНИЯ
        var playerData: String? = null
        val sql = "SELECT UUID FROM bank_wallets WHERE id = ?"

        try {
            val result = dbManager.executeQuery(sql, id)
            if (result.isNotEmpty()) {
                val row = result.firstOrNull()
                val uuid = row?.get("UUID") as? String
                if (uuid != null) {
                    val playerDataSql = "SELECT PlayerName, DiscordID, Registration, `2f Auth`, USDT, Level FROM bank_users WHERE UUID = ?"
                    try {
                        val result2 = dbManager.executeQuery(playerDataSql, uuid)
                        if (result2.isNotEmpty()) {
                            val row2 = result2.firstOrNull()
                            val playerName = row2?.get("PlayerName") as? String
                            val discordId = row2?.get("DiscordID") as? String
                            val registration = row2?.get("Registration") as? String
                            val usdt = row2?.get("USDT") as? Int
                            val level = row2?.get("Level") as? Int
                            playerData = "id: $id $playerName : $discordId : $registration : $usdt : $level"
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


}