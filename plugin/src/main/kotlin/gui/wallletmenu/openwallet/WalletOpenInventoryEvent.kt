package gui.wallletmenu.openwallet

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletOpenInventoryEvent(config: FileConfiguration, private val discordBot: DiscordBot) : Listener {
    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(discordBot.getJDA(), config)
    //private val countAccountConfig = config.getInt("count-free-accounts") TODO: Вернуть в будущем когда будет система разных кошельков.
    private val countAccountConfig = 1
    private val priceAccountConfig = config.getInt("price-account")
    private val currencyAccountConfig = config.getString("currency-block-default")
    private val verificationAccountConfig = config.getBoolean("checker-banker")
    private val channelIdBankerNotifier = config.getString("channel-id-banker")
    private val channelIdLogger = config.getString("channel-id-logger") ?: "null"

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.HOPPER) {
            val title = e.view.title()
            val expectedTitle = localizationManager.getMessage("localisation.inventory.title.wallet-open-confirmation")
            if (functions.isComponentEqual(title, expectedTitle)) { //todo: сделать сообщение из конфига
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleAccept = localizationManager.getMessage("localisation.inventory.item.accept")
                    val titleReject = localizationManager.getMessage("localisation.inventory.item.reject")
                    if (functions.isComponentEqual(displayNameComponent, titleAccept)) {
                        val walletCurrency = currencyAccountConfig
                        if (walletCurrency == null){
                            player.sendMessage("ошибка валюты")
                            return
                        }
                        val currency = functions.convertStringToMaterial(walletCurrency)
                        val typeBlock : Material
                        if (currency.second) {
                            typeBlock = currency.first!! //Преобразованный материал
                        } else {
                            player.sendMessage("Ошибка иницилизации валюты")
                            return

                        }
                        val blocksInInventory = functions.countBlocksInInventory(player, typeBlock)
                        if (blocksInInventory < priceAccountConfig){
                            player.sendMessage("Недостаточно средств")
                            return
                        }
                        val uuidPlayer = player.uniqueId.toString()
                        val countAccounts = walletDB.getWalletsCount(uuidPlayer)
                        if (countAccounts < countAccountConfig){
                            var price = priceAccountConfig
                            var verificationInt = 0
                            if (!verificationAccountConfig){
                                verificationInt = 1
                                price = 0
                                //TODO:Добавить зачисление price куда-то пока не знаю куда.
                            }else{
                                val lastID = walletDB.getLastIDWalletFree().toString()
                                val discordID = userDB.getDiscordIDbyUUID(player.uniqueId.toString())
                                val mention = discordBot.getMentionUser(discordID.toString())
                                if (discordID != null){
                                    discordNotifier.sendMessageChannel(
                                        channelIdBankerNotifier.toString(),
                                        "/././././././././././././././././\n" + //todo: сделать сообщение из конфига
                                                "Пришел новый запрос на открытие кошелька!\n" +
                                                "Пользователь - `${player.name}`\n" +
                                                "Дискорд - $mention\n" +
                                                "Номер кошелька - `$lastID`\n" +
                                                "/././././././././././././././././"
                                    )

                                    discordNotifier.sendMessageWithButtons(
                                        channelIdBankerNotifier.toString(),
                                        "Вам необходимо подтвердить или отклонить запрос.", //todo: сделать сообщение из конфига
                                        lastID
                                    )
                                }else {
                                    // Обработка случая, когда discordID == null
                                    functions.sendMessagePlayer(player, "Не удалось найти идентификатор Discord.") //todo: сделать сообщение из конфига
                                }
                            }
                            walletDB.insertWallet(player, currencyAccountConfig!!, price, verificationInt).thenAccept { isCreate ->
                                if (isCreate) {

                                    val currency = functions.convertStringToMaterial(currencyAccountConfig)
                                    val typeBlock : Material
                                    if (currency.second) {
                                        typeBlock = currency.first!! //Преобразованный материал
                                    } else {
                                        player.sendMessage("Ошибка иницилизации валюты")
                                        return@thenAccept

                                    }
                                    functions.takeItem(player, typeBlock, priceAccountConfig)
                                    discordNotifier.sendMessageChannelLog(localizationManager.getMessage("localisation.discord.logger.open-successfully"))
                                    //functions.sendMessagePlayer(player, "Банковский счет был успешно создан!")
                                } else {
                                    discordNotifier.sendMessageChannelLog(localizationManager.getMessage("localisation.discord.logger.open-unsuccessfully"))
                                    functions.sendMessagePlayer(player, "Не удалось создать банковский счет.") //todo: сделать сообщение из конфига
                                }
                            }
                            functions.sendMessagePlayer(player, "Банковский счет был успешно создан!") //todo: сделать сообщение из конфига
                        }else{
                            functions.sendMessagePlayer(player, "У вас уже максимальное количество счетов!") //todo: сделать сообщение из конфига
                        }
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleReject)) {
                        functions.sendMessagePlayer(player, "Отклонено.")
                    } //todo: сделать сообщение из конфига
                    return
                }
            }
        }
    }

}