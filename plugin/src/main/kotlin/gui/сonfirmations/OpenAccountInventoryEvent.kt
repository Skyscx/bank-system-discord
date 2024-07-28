package gui.сonfirmations

import database.Database
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class OpenAccountInventoryEvent(private val database: Database, config: FileConfiguration, discordBot: DiscordBot) : Listener {
    private val functions = Functions()
    //private val functionsDiscord = FunctionsDiscord(discordBot)
    private val discordBot = DiscordBot.getInstance(database, config)
    private val discordNotifier = DiscordNotifier(discordBot.getJDA())
    private val countAccountConfig = config.getInt("count-free-accounts")
    private val priceAccountConfig = config.getInt("price-account")
    private val currencyAccountConfig = config.getString("currency-block-default")
    private val verificationAccountConfig = config.getBoolean("checker-banker")
    private val channelIdBankerNotifier = config.getString("channel-id-banker")
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.HOPPER) {
            if (e.view.title == "Подтверждение операции") {
                //accept
                if (e.currentItem!!.itemMeta.displayName == "Подтвердить!") {
                    if (functions.hasDiamondOre(player)){
                        val uuidPlayer = player.uniqueId.toString()
                        val countAccounts = database.getAccountCount(uuidPlayer)
                        if (countAccounts < countAccountConfig){
                            var price = priceAccountConfig
                            var verificationInt = 0
                            if (!verificationAccountConfig){
                                verificationInt = 1
                                price = 0
                                //TODO:Добавить зачисление price куда-то пока не знаю куда.
                            }else{
                                val lastID = database.getLastID().toString()
                                val discordID = database.getUUIDbyDiscordID(player.uniqueId.toString())
                                val mention = discordBot.mentionUserById(discordID.toString())
                                //val mention = functionsDiscord.mentionUserById(uuidPlayer)
                                discordNotifier.sendMessageChannel(
                                    channelIdBankerNotifier.toString(),
                                    "/././././././././././././././././\n" +
                                            "Пришел новый запрос на открытие кошелька!\n" +
                                            "Пользователь - ${player.name}\n" +
                                            "Дискорд - $mention'\n" +
                                            "Номер кошелька - $lastID\n" +
                                            "/././././././././././././././././"
                                )

                                discordNotifier.sendMessageWithButtons(
                                    channelIdBankerNotifier.toString(),
                                    "Вам необходимо подтвердить или отклонить запрос.",
                                    lastID
                                )
                            }
                            database.insertAccount(player,currencyAccountConfig!!, price, verificationInt)
                            functions.sendMessagePlayer(player, "Банковский счет был успешно создан!")
                        }else{
                            functions.sendMessagePlayer(player, "У вас уже максимальное количество счетов!")
                        }
                    }else{
                        functions.sendMessagePlayer(player,"Недостаточно алмазной руды на руках!")
                    }
                }
                //close
                if (e.currentItem!!.itemMeta.displayName == "Отклонить!") {
                    functions.sendMessagePlayer(player, "Отклонено.")
                }
                player.closeInventory()
                return
            }
        }
    }

}