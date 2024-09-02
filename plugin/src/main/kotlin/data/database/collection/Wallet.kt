package data.database.collection

import App
import App.Companion.historyDB
import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import data.database.DatabaseManager
import discord.FunctionsDiscord
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture

class Wallet (
    private val dbManager: DatabaseManager,
    private val plugin: App,
    private val functionsDiscord: FunctionsDiscord
) {
    /**
     * Создание кошелька в таблице кошельков
     */
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
                val idWallet = walletDB.getLastIDWalletFree() ?: 0
                val result = dbManager.executeUpdate(
                    sql,
                    playerUUID.toString(), discordID!!, currentDate, privateKey, balance, currency, name,
                    verificationInt, amount, inspector, dateVerification, 1
                )
                if (result){
                    userDB.setDefaultWalletByUUID(playerUUID.toString(), idWallet)
                }
                future.complete(result)
            } catch (e: SQLException) {
                e.printStackTrace()
                future.complete(false)
            }
        })

        return future
    }

    /**
     * Получение статуса верификации кошелька в таблице кошельков
     *
     * 1 - одобрен || 0 - ожидание || -1 - отказан
     */
    fun getVerificationWallet(id: Int): Int? {
        val sql = "SELECT Verification FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            return row?.get("Verification") as? Int
        }
        return null
    }

    /**
     * Присвоение статуса верификации кошелька в таблице кошельков
     *
     * 1 - одобрен || 0 - ожидание || -1 - отказан
     */
    fun setVerificationWallet(id: Int, verification: Int): Boolean {
        val sql = "UPDATE bank_wallets SET Verification = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, verification, id)
    }

    /**
     * Получение DiscordID проверяющего по ID в таблице кошельков.
     */
    fun getInspectorWallet(id: Int): String? {
        val sql = "SELECT Inspector FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("Inspector") as? String
    }

    /**
     * Присвоение записи о проверяющей в виде DiscordID в таблице кошельков по ID.
     */
    fun setInspectorWallet(id: Int, inspector: String): Boolean {
        val sql = "UPDATE bank_wallets SET Inspector = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, inspector, id)
    }

    /**
     * Получение даты верификации в формате dd:MM:yyyy HH:mm:ss по ID кошельку в таблице кошельков.
     */
    fun getVerificationWalletDate(id: Int): String? {
        val sql = "SELECT VerificationDate FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("VerificationDate") as? String
    }

    /**
     * Присвоение записи даты верификации в формате dd:MM:yyyy HH:mm:ss по ID кошельку в таблице кошельков.
     */
    fun setVerificationWalletDate(id: Int): Boolean {
        val currentDate = SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Date())
        val sql = "UPDATE bank_wallets SET VerificationDate = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, currentDate, id)
    }

    /**
     * Установление значения депозита в таблице кошельков по ID кошелька.
     */
    fun setDepositWallet(id: Int, deposit: String): Boolean {
        val sql = "UPDATE bank_wallets SET Deposit = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, deposit, id)
    }

    /**
     * Получение значение депозита по ID кошельку из таблицы кошельков.
     */
    fun getDepositWallet(id: Int): Int? {
        var deposit: Int? = null
        val sql = "SELECT Deposit FROM bank_wallets WHERE ID = ?"

        try {
            val result = dbManager.executeQuery(sql, id)
            if (result.isNotEmpty()) {
                val row = result.firstOrNull()
                deposit = row?.get("Deposit") as? Int
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return deposit
    }

    /**
     * Получение UUID пользователя по ID пользователю из таблицы кошельков.
     */
    fun getUUIDbyWalletID(id: Int): String? {
        val sql = "SELECT UUID FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("UUID") as? String
    }

    /**
     * Получение Игрока по ID WALLET из таблицы кошельков.
     */
    fun getPlayerByWalletID(id: Int): Player? {
        val sql = "SELECT UUID FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        val uuid = row?.get("UUID") as? String
        return uuid?.let { Bukkit.getPlayer(UUID.fromString(it)) }
    }

    /**
     * Получение баланса игрока из базы данных
     */
    fun getWalletBalance(walletID: Int): Int? {
        val sql = "SELECT Balance FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, walletID)
        if (result.isEmpty()) return null
        val row = result.firstOrNull()
        return row?.get("Balance") as? Int
    }

    /**
     * Обновление баланса кошелька по ID кошелька. (NEW)
     */
    fun updateWalletBalance(id: Int, amount: Int): Boolean {
        val sql = "UPDATE bank_wallets SET balance = balance + ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, amount, id)
    }

    // NEW ДЛЯ ПРОВЕРОК СТАТУСА
    fun checkWalletStatus(walletID: Int): Boolean {
        val sql = "SELECT Status FROM bank_wallets WHERE ID = ?"
        var status = false
        val result = dbManager.executeQuery(sql, walletID)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            status = (row?.get("Status") as? Int) == 1
        }
        return status
    }

    /**
     * Метод перевода со счета на счет.
     */
    fun transferCash(
        sender: Player,
        target: String,
        senderWalletID: Int,
        targetWalletID: Int,
        amount: Int,
        currency: String,
        status: Int,
        uuidSender: String,
        uuidTarget: String
    ): Boolean {
        updateWalletBalance(senderWalletID, -amount)
        updateWalletBalance(targetWalletID, amount)

        historyDB.insertBankHistory(sender, target, senderWalletID, targetWalletID, amount, currency, status, uuidSender, uuidTarget)
        return true
    }

    /**
     * Счетчик количества кошельков пользователя по UUID в таблице кошельков
     */
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

    /**
     * Получение ID кошелька по имени кошелька.
     */
    fun getIDByWalletName(name: String): Int? {
        val sql = "SELECT ID FROM bank_wallets WHERE Name = ?"
        val result = dbManager.executeQuery(sql, name)
        val row = result.firstOrNull()
        val id = row?.get("ID") as? Int
        return id
    }

    /**
     * Преобразование идентификатора кошелька по названию или уникальному идентификатору на уникальный идентификатор
     *
     * В метод отправляется аргумент.
     * Если этот аргумент число - то это и есть уникальный идентификатор.
     * Если этот аргумент строка - то это название, и происходит преобразование названия в уникальный идентификатор.
     */
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
        val sql = "SELECT * FROM bank_wallets WHERE Verification = 0 AND Status != 0 ORDER BY id ASC LIMIT 5"

        val result = dbManager.executeQuery(sql)
        for (row in result) {
            val id = row["id"] as Int
            unverifiedAccounts.add(id.toString())
        }

        return unverifiedAccounts
    }

    /**
     * Генерация строки с необходимыми данными о статусе верификации кошелька по ID кошельку из таблицы кошельков.
     */
    fun getPlayerDataByID(id: Int): String? { //TODO: РАЗЪЕДЕНИТЬ ОБРАЩЕНИЯ
        var playerData: String? = null
        val sql = "SELECT UUID FROM bank_wallets WHERE id = ?"

        val result = dbManager.executeQuery(sql, id)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            val uuid = row?.get("UUID") as? String
            if (uuid != null) {
                val playerDataSql =
                    "SELECT PlayerName, DiscordID, Registration, `2f Auth`, USDT, Level FROM bank_users WHERE UUID = ?"
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

        return playerData
    }

    /**
     * Генерация строки с необходимыми данными о статусе верификации кошелька по ID кошельку из таблицы кошельков.
     */
    fun getWalletDataByID(id: Int): String? {
        var walletData: String? = "message -> no info"
        val sql = "SELECT Name, Currency, Balance FROM bank_wallets WHERE ID = ?"
        val result = dbManager.executeQuery(sql, id)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            val name = row?.get("Name") as? String ?: "Unknown"
            val currency = row?.get("Currency") as? String ?: "Unknown"
            val balance = row?.get("Balance") as? String ?: "0"
            walletData = localizationManager.getMessage(
                "localisation.messages.generate.wallet-data",
                "name" to name,
                "walletID" to id.toString(),
                "balance" to balance,
                "currency" to currency
            )
        }
        return walletData
    }

    /**
     * Проверка по ID есть ли у пользователя доступный депозит для вывода (Возвращение boolean)
     */
    fun isDepositWalletAvailable(id: Int): Boolean {
        var deposit: Int? = null
        val verification = getVerificationWallet(id)

        if (verification == -1) {
            val sql = "SELECT Deposit FROM bank_wallets WHERE ID = ?"
            val result = dbManager.executeQuery(sql, id)
            if (result.isNotEmpty()) {
                val row = result.firstOrNull()
                deposit = row?.get("Deposit") as? Int
            }
        }

        return deposit != null
    }

    /**
     * Получение списка с ID кошельками, которые не одобрили, из таблицы кошельков.
     */
    fun getIdsWalletsReturnDepositByUUID(uuid: String): List<Int> {
        val depositIds = mutableListOf<Int>()
        val sql = "SELECT ID FROM bank_wallets WHERE UUID = ? AND Verification = -1 AND Deposit != 0"

        val result = dbManager.executeQuery(sql, uuid)
        for (row in result) {
            val id = row["ID"]
            if (id != null) {
                depositIds.add(id as Int)
            }
        }

        return depositIds
    }

    /**
     * Получение списка с ID кошельками, которые существуют у пользователя с идентичным UUID
     */
    fun getIdsWalletsOwnerByUUID(uuid: String): List<Int> {
        val depositIds = mutableListOf<Int>()
        val sql = "SELECT ID FROM bank_wallets WHERE UUID = ? AND Status != 0"

        val result = dbManager.executeQuery(sql, uuid)
        for (row in result) {
            val id = row["ID"] as Int
            depositIds.add(id)
        }

        return depositIds
    }

    /**
     * Сохранение номера кошелька как основного кошелька для транзакций.
     */
    fun setDefaultWalletID(uuid: String?, walletID: Int) {
        val sql = "UPDATE bank_users SET DefaultWalletID = ? WHERE UUID = ?"

        dbManager.executeUpdate(sql, walletID, uuid!!)
    }

    /**
     * Получение основного кошелька для транзакций по UUID
     * todo: УДАЛИТЬ
     */
    fun getDefaultWalletIDByUUID(uuid: String): Int? {
        var defaultWalletID: Int? = null
        val sql = "SELECT DefaultWalletID FROM bank_users WHERE UUID = ?"

        val result = dbManager.executeQuery(sql, uuid)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            defaultWalletID = row?.get("DefaultWalletID") as? Int
        }

        return defaultWalletID
    }

    /**
     * Получение валюты кошелька по Wallet ID.
     */
    fun getWalletCurrency(walletID: Int): String? {
        var currency: String? = null
        val sql = "SELECT Currency FROM bank_wallets WHERE ID = ?"

        val result = dbManager.executeQuery(sql, walletID)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            currency = row?.get("Currency") as? String
        }

        return currency
    }

    /**
     * Удаление кошелька из таблицы кошельков по ID кошельку.
     */
    fun deleteUserWallet(id: Int): Boolean {
        val sqlUpdateWallet = "UPDATE bank_wallets SET Name = 'NULL', Deposit = 0, Status = 0 WHERE ID = ?"
        //val sqlUpdateUser = "UPDATE bank_users SET DefaultWalletID = 0 WHERE DefaultWalletID = ?"

        return try {
            // Получаем UUID по ID
            val uuid = getUUIDbyWalletID(id).toString()
            //todo: добавить проверки на дб
            userDB.setDefaultWalletByUUID(uuid, -1)
//            if (uuid != null) {
//                // Получаем DefaultIDWallet по UUID
//                val defaultIDWallet = getDefaultWalletIDByUUID(uuid)
//                if (defaultIDWallet != null && defaultIDWallet == id) {
//                    // Обновляем DefaultIDWallet в таблице bank_users
//                    dbManager.executeUpdate(sqlUpdateUser, id)
//                }
//            }

            // Обновляем кошелек в таблице bank_wallets
            dbManager.executeUpdate(sqlUpdateWallet, id)
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Получение последнего не использованного ID кошелька.
     */
    fun getLastIDWalletFree(): Int? {
        var lastId: Int? = null
        val sql = "SELECT MAX(ID) FROM bank_wallets"

        val result = dbManager.executeQuery(sql)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            lastId = (row?.get("MAX(ID)") as? Int)?.plus(1)
            if (lastId == null) lastId = 1
        }
        return lastId
    }

    /**
     * Проверка существует ли данный ID кошелек в таблице кошельков.
     */
    fun doesIdExistWallet(id: Int): Boolean {
        val sql = "SELECT 1 FROM bank_wallets WHERE ID = ?"

        val result = dbManager.executeQuery(sql, id)
        return result.isNotEmpty()
    }

    /**
     * Проверка существования UUID в таблице кошельков
     */
    fun doesUUIDWalletsExist(uuid: String): Boolean {
        val sql = "SELECT COUNT(*) FROM bank_wallets WHERE UUID = ?"
        var exists = false

        val result = dbManager.executeQuery(sql, uuid)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            val count = row?.get("COUNT(*)") as? Int
            exists = count != null && count > 0
        }

        return exists
    }

    /**
     * Проверка на отключенный кошелек. todo: выше похожий метод
     */
    fun isWalletStatusZero(walletID: Int): Boolean {
        val sql = "SELECT Status FROM bank_wallets WHERE ID = ?"
        var status: Int? = null

        val result = dbManager.executeQuery(sql, walletID)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            status = row?.get("Status") as? Int
        }

        return status == 0
    }

    /**
     * Проверка на то существует ли уже кошелек с таким названием.
     */
    fun isWalletNameAvailable(walletName: String): Boolean {
        val sql = "SELECT COUNT(*) FROM bank_wallets WHERE Name = ?"
        var count: Int? = null

        val result = dbManager.executeQuery(sql, walletName)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            count = row?.get("COUNT(*)") as? Int
        }

        return count == 0
    }

    /**
     * Получение имени кошелька по ID WALLET
     */
    fun getNameWalletByIDWallet(id: Int): String {
        val sql = "SELECT Name FROM bank_wallets WHERE ID = ?"
        var name = ""

        val result = dbManager.executeQuery(sql, id)
        if (result.isNotEmpty()) {
            val row = result.firstOrNull()
            name = row?.get("Name") as? String ?: ""
        }

        return name
    }

    /**
     * Присвоение нового имени кошелька по ID WALLET
     */
    fun setNameWalletByIDWallet(name: String?, id: Int): Boolean {
        val sql = "UPDATE bank_wallets SET Name = ? WHERE ID = ?"
        return dbManager.executeUpdate(sql, name!!, id)
    }

    companion object {
        @Volatile
        private var instance: Wallet? = null

        fun getInstance(dbManager: DatabaseManager, plugin: App, functionsDiscord: FunctionsDiscord): Wallet =
            instance ?: synchronized(this) {
                instance ?: Wallet(dbManager, plugin, functionsDiscord).also { instance = it }
            }
    }

}


