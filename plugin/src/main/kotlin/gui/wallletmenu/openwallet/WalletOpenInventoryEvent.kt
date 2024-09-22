package gui.wallletmenu.openwallet

import App.Companion.configPlugin
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletOpenInventoryEvent(config: FileConfiguration, private val discordBot: DiscordBot) : Listener {
    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    //private val countAccountConfig = config.getInt("count-free-accounts") TODO: Вернуть в будущем когда будет система разных кошельков.
    private val countAccountConfig = 1
    private val priceAccountConfig = config.getInt("price-account")
    private val currencyAccountConfig = config.getString("currency-block-default")
    private val verificationAccountConfig = config.getBoolean("checker-banker")

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.HOPPER) {
            val title = e.view.title()
            val expectedTitle = "localisation.inventory.title.wallet-open-confirmation".localized()
            if (functions.isComponentEqual(title, expectedTitle)) {
                val currentItem = e.currentItem ?: return
                val itemMeta = currentItem.itemMeta ?: return
                if (itemMeta.hasDisplayName()) {
                    e.isCancelled
                    player.closeInventory()
                    val displayNameComponent = itemMeta.displayName() ?: return
                    val titleAccept = "localisation.inventory.item.accept".localized()
                    val titleReject = "localisation.inventory.item.reject".localized()
                    if (functions.isComponentEqual(displayNameComponent, titleAccept)) {
                        val walletCurrency = currencyAccountConfig ?: return
                        val currency = functions.convertStringToMaterial(walletCurrency)
                        val typeBlock : Material
                        if (currency.second) {
                            typeBlock = currency.first!!
                        } else return
                        val blocksInInventory = functions.countBlocksInInventory(player, typeBlock)
                        if (blocksInInventory < priceAccountConfig){
                            player.sendMessage("localisation.error.not-player-blocks".localized())
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
                                    val channelIdBankerNotifier = configPlugin.getString("channel-id-banker") ?: return

                                    discordNotifier.sendEmbedMessageAndButtons(
                                        channelId = channelIdBankerNotifier,
                                        color = 1,
                                        buttons = listOf(
                                            Button.of(ButtonStyle.SUCCESS, "acceptAccount:$lastID", "localisation.discord.embed.report.buttons.approve".localized()),
                                            Button.of(ButtonStyle.DANGER, "rejectAccount:$lastID", "localisation.discord.embed.report.buttons.reject".localized())
                                        ),
                                        title = "localisation.discord.embed.title.verification.dispatch".localized(),
                                        description = "localisation.discord.embed.verification.description.choice".localized(),
                                        fields = listOf(
                                            MessageEmbed.Field("localisation.discord.embed.verification.field.user".localized(), player.name, false),
                                            MessageEmbed.Field("localisation.discord.embed.verification.field.discord".localized(), mention, false),
                                            MessageEmbed.Field("localisation.discord.embed.verification.field.walletID".localized(), lastID, false)
                                            )


                                    )
                                }else {
                                    functions.sendMessagePlayer(player, "localisation.error.receiver-not-found".localized())
                                }
                            }
                            val type = "DEFAULT"
                            walletDB.insertWallet(player, currencyAccountConfig, price, verificationInt, type).thenAccept { isCreate ->
                                if (isCreate) {

                                    val currency = functions.convertStringToMaterial(currencyAccountConfig)
                                    val typeBlock : Material
                                    if (currency.second) {
                                        typeBlock = currency.first!! //Преобразованный материал
                                    } else return@thenAccept
                                    functions.takeItem(player, typeBlock, priceAccountConfig)
                                    //functions.sendMessagePlayer(player, "Банковский счет был успешно создан!")
                                } else {
                                    discordNotifier.sendMessageChannelLog("localisation.discord.logger.open-unsuccessfully".localized())
                                    functions.sendMessagePlayer(player, "localisation.messages.out.wallet.open.unsuccessful".localized())
                                }
                            }
                            if (verificationInt == 1){
                                functions.sendMessagePlayer(player, "localisation.messages.out.wallet.open.successful".localized())
                                discordNotifier.sendMessageChannelLog("localisation.discord.logger.open-successfully".localized( "player" to player.name))
                            } else {
                                functions.sendMessagePlayer(player, "localisation.messages.out.wallet.wait-banker.wait".localized())
                            }
                        }else{
                            functions.sendMessagePlayer(player, "localisation.messages.out.wallet.open.is-max".localized())
                        }
                    }
                    if (functions.isComponentEqual(displayNameComponent, titleReject)) {
                        functions.sendMessagePlayer(player, "localisation.messages.out.wallet.open.cancel".localized())
                    }
                    return
                }
            }
        }
    }


}