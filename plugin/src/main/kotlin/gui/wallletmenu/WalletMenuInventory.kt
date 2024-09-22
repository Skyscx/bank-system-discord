package gui.wallletmenu

import App.Companion.configPlugin
import App.Companion.localized
import App.Companion.userDB
import App.Companion.walletDB
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class WalletMenuInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    private val title = Component.text("localisation.inventory.title.menu-wallet".localized())
    private val priceDefaultWallet = configPlugin.getInt("price-account").toString()
    override fun createInventory(player: Player): Inventory {

        val uuid = player.uniqueId.toString()
        val walletDefault = userDB.getDefaultWalletByUUID(uuid) ?: -1
        val successful = walletDB.getVerificationWallet(walletDefault)
        val inventory: Inventory = when (successful) {
            0 -> createFailureInventory()
            1 -> createSuccessInventory()
            else -> createDefaultInventory()
        }
        player.openInventory(inventory)
        return inventory
    }

    private fun createDefaultInventory(): Inventory {
        val inventory = Bukkit.createInventory(null, 54, title) //todo: если не создан
        // Кнопка для профиля
        val profile = systemGUI.createItem(
            Material.PLAYER_HEAD,
            "localisation.inventory.item.profile".localized(),
            listOf("localisation.inventory.lore.profile.menu.empty".localized()),
            1
        )
        // Кнопка для создания кошелька
        val openWallet = systemGUI.createItem(
            Material.GREEN_WOOL,
            "localisation.inventory.item.open-wallet".localized(),
            listOf("localisation.inventory.lore.open-wallet.menu".localized( "amount" to priceDefaultWallet)),
            1
        )
//        TODO: Перенести в отдельное меню -> INFO
//        // Кнопка для гайда
//        val guid = systemGUI.createItem(
//            Material.BOOKSHELF,
//            localizationManager.getMessage("localisation.inventory.item.guid-book"),
//            listOf(localizationManager.getMessage("localisation.inventory.lore.guid-book.menu")),
//            1
//        )
//        // Кнопка для Репорта
//        val report = systemGUI.createItem(
//            Material.FIRE_CHARGE,
//            localizationManager.getMessage("localisation.inventory.item.report"),
//            listOf(localizationManager.getMessage("localisation.inventory.lore.report.menu")),
//            1
//        )
//        //Кнопка связи с банкиром
//        val sendMessageBanker = systemGUI.createItem(
//            Material.WRITABLE_BOOK,
//            localizationManager.getMessage("localisation.inventory.item.send-banker-message"),
//            listOf(localizationManager.getMessage("localisation.inventory.lore.send-banker-message.menu")),
//            1
//        )
        //Заполнитель по бокам
        val filler = systemGUI.createItem(
            Material.GREEN_STAINED_GLASS_PANE,
            "·",
            customModelData = 1
        )
        // Кнопка информации
        val info = systemGUI.createItem(
            Material.TORCH,
            "localisation.inventory.item.info".localized(),
            listOf("localisation.inventory.lore.wait-banker.menu".localized()),
            1
        )

        inventory.setItem(10, profile)
        inventory.setItem(12, filler)
        inventory.setItem(13, openWallet)
        inventory.setItem(14, filler)
        inventory.setItem(16, info)
        return inventory
    }

    private fun createSuccessInventory(): Inventory {
        val inventory = Bukkit.createInventory(null, 54, title) //todo: если создан
        // Кнопка для профиля
        val profile = systemGUI.createItem(
            Material.PLAYER_HEAD,
            "localisation.inventory.item.profile".localized(),
            listOf("localisation.inventory.lore.profile.menu".localized( "amount" to "[todo AMOUNT]")),
            1
        )
        val closeWallet = systemGUI.createItem(
            Material.RED_WOOL,
            "localisation.inventory.item.close-wallet".localized(),
            listOf("localisation.inventory.lore.close-wallet.menu".localized()),
            1
        )
        // Кнопка для гайда
        val guid = systemGUI.createItem(
            Material.BOOKSHELF,
            "localisation.inventory.item.guid-book".localized(),
            listOf("localisation.inventory.lore.guid-book.menu".localized()),
            1
        )
        // Кнопка для Репорта
        val report = systemGUI.createItem(
            Material.FIRE_CHARGE,
            "localisation.inventory.item.report".localized(),
            listOf("localisation.inventory.lore.report.menu".localized()),
            1
        )
        //Кнопка связи с банкиром
        val sendMessageBanker = systemGUI.createItem(
            Material.WRITABLE_BOOK,
            "localisation.inventory.item.send-banker-message".localized(),
            listOf("localisation.inventory.lore.send-banker-message.menu".localized()),
            1
        )
        //Кнопка для действий с кошельком
        val actionsWallet = systemGUI.createItem(
            Material.PURPLE_WOOL,
            "localisation.inventory.item.actions".localized(),
            listOf("localisation.inventory.lore.actions.menu".localized()),
            2
        )
        // Кнопка для просмотра истории транзакций todo: Локализацию
        val history = systemGUI.createItem(
            Material.PAPER,
            "localisation.inventory.item.history".localized(),
            listOf("localisation.inventory.lore.history.menu".localized()),
            5
        )
        //Кнопка перевода
        val transfer = systemGUI.createItem(
            Material.CYAN_WOOL,
            "localisation.inventory.item.transfer".localized(),
            listOf("localisation.inventory.lore.transfer.menu".localized()),
            1
        )


        //Список штрафов DEV
        val fineList = systemGUI.createItem(
            Material.PINK_GLAZED_TERRACOTTA,
            "localisation.inventory.item.fine-list".localized(),
            listOf("localisation.inventory.lore.fine-list.menu".localized()),
            1
        )
        //Оплатить штраф DEV
        val payFine = systemGUI.createItem(
            Material.GREEN_GLAZED_TERRACOTTA,
            "localisation.inventory.item.pay-fine".localized(),
            listOf("localisation.inventory.lore.appeal-fine.menu".localized()),
            1
        )
        //Обажаловать штраф DEV
        val appealFine = systemGUI.createItem(
            Material.RED_GLAZED_TERRACOTTA,
            "localisation.inventory.item.appeal-fine".localized(),
            listOf("localisation.inventory.lore.appeal-fine.menu".localized()),
            1
        )

        //Список  счетов DEV
        val billList = systemGUI.createItem(
            Material.ORANGE_GLAZED_TERRACOTTA,
            "localisation.inventory.item.bill-list".localized(),
            listOf("localisation.inventory.lore.bill-list.menu".localized()),
            1
        )
        //Оплатить счет DEV
        val payBill = systemGUI.createItem(
            Material.MAGENTA_GLAZED_TERRACOTTA,
            "localisation.inventory.item.pay-bill".localized(),
            listOf("localisation.inventory.lore.pay-bill.menu".localized()),
            1
        )
        //Выставить счет DEV
        val putBill = systemGUI.createItem(
            Material.YELLOW_GLAZED_TERRACOTTA,
            "localisation.inventory.item.put-bill".localized(),
            listOf("localisation.inventory.lore.put-bill.menu".localized()),
            1
        )

        inventory.setItem(10, profile)
        inventory.setItem(16, closeWallet)

//        todo: Включить когда будет работать
        inventory.setItem(39, guid)
        inventory.setItem(40, report)
//        inventory.setItem(41, sendMessageBanker)

        inventory.setItem(12, actionsWallet)
        inventory.setItem(13, history)
        inventory.setItem(14, transfer)

//        todo: Включить когда будет работать
//        inventory.setItem(28, fineList)
//        inventory.setItem(37, payFine)
//        inventory.setItem(46, appealFine)
//
//        inventory.setItem(34, billList)
//        inventory.setItem(43, payBill)
//        inventory.setItem(52, putBill)

        return inventory
    }

    private fun createFailureInventory(): Inventory {
        val inventory = Bukkit.createInventory(null, 27, title) //todo: если ожидание
        // Кнопка профиля
        val profile = systemGUI.createItem(
            Material.PLAYER_HEAD,
            "localisation.inventory.item.profile".localized(),
            listOf("localisation.inventory.lore.profile.menu".localized()),
            1
        )
        // Информация ожидания
        val waitBanker = systemGUI.createItem(
            Material.YELLOW_WOOL,
            "localisation.inventory.item.wait-banker".localized(),
            listOf("localisation.inventory.lore.wait-banker.menu".localized()),
            1
        )
        //Заполнитель по бокам
        val filler = systemGUI.createItem(
            Material.YELLOW_STAINED_GLASS_PANE,
            "·",
            customModelData = 1
        )
        // Кнопка информации
        val info = systemGUI.createItem(
            Material.TORCH,
            "localisation.inventory.item.info".localized(),
            listOf("localisation.inventory.lore.info.menu".localized()),
            1
        )

        inventory.setItem(10, profile)
        inventory.setItem(12, filler)
        inventory.setItem(13, waitBanker)
        inventory.setItem(14, filler)
        inventory.setItem(16, info)

        return inventory
    }
}