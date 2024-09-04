package gui.wallletmenu.closewallet

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import discord.dsbot.DiscordBot
import discord.dsbot.DiscordNotifier
import functions.Functions
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class WalletCloseInventoryEvent(config: FileConfiguration, discordBot: DiscordBot) : Listener{
    private val functions = Functions()
    private val discordNotifier = DiscordNotifier(config)
    //private val countAccountConfig = config.getInt("count-free-accounts") TODO: Вернуть в будущем когда будет система разных кошельков.


    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (e.view.type == InventoryType.HOPPER) {
            val title = e.view.title()
            val expectedTitle = localizationManager.getMessage("localisation.inventory.title.wallet-close-confirmation")
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
                        val uuid = player.uniqueId.toString()
                        val walletID = userDB.getDefaultWalletByUUID(uuid) ?: return
                        val balance = walletDB.getWalletBalance(walletID) ?: 0
                        if (balance < 0) {
                            player.sendMessage("Вы не можете закрыть счет с отрицательным балансом.")
                            return
                        }
                        val currency = walletDB.getWalletCurrency(walletID).toString()
                        val successful = walletDB.deleteUserWallet(walletID)
                        if (successful){
                            player.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.remove-successfully.sender"))
                            discordNotifier.sendMessageChannelLog(localizationManager.getMessage("localisation.discord.logger.remove-successfully",
                                "player" to player.name, "amount" to balance.toString(), "currency" to currency))
                        } else {
                            player.sendMessage(localizationManager.getMessage("localisation.messages.out.wallet.remove-unsuccessfully.sender"))
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