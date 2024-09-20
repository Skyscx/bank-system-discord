package functions.events

import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack


class PlayerConnection(config: FileConfiguration, discordBot: DiscordBot) : Listener{
    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    private val channelIdLogger = config.getString("channel-id-logger") ?: "null"



    @EventHandler
    fun onPlayerConnect(event: PlayerJoinEvent){
        val player = event.player
        val playerUUID = player.uniqueId
        userDB.checkPlayerTaskInsert(playerUUID)
        val idList = walletDB.getIdsWalletsReturnDepositByUUID(playerUUID.toString())
        for (id in idList){
            if (walletDB.isDepositWalletAvailable(id)) {
                val deposit = walletDB.getDepositWallet(id)
                val currency = walletDB.getWalletCurrency(id) //todo: реализовать визуальное отображение название валюты
                val item = ItemStack(Material.DIAMOND_ORE) // TODO:Брать из конфигурации

                if (functions.giveItem(player, item, deposit!!)) {
                    val message = "localisation.messages.out.wallet.back-deposit".localized(
                        "amount" to deposit.toString(), "currency" to item.type.name)
                    player.sendMessage(message)
                    discordNotifier.sendMessageChannelLog("localisation.discord.logger.back-deposit".localized(
                        "playerName" to player.name, "amount" to deposit.toString(), "currency" to currency.toString()))
                    walletDB.deleteUserWallet(id)
                }


            }
        }

    }


}