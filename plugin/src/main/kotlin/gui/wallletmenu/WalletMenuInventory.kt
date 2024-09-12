package gui.wallletmenu

import App.Companion.localizationManager
import App.Companion.userDB
import App.Companion.walletDB
import gui.InventoryCreator
import gui.SystemGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class WalletMenuInventory : InventoryCreator {
    private val systemGUI = SystemGUI()
    private val title = Component.text(localizationManager.getMessage("localisation.inventory.title.menu-wallet"), TextColor.color(6, 178, 0))
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
            localizationManager.getMessage("localisation.inventory.item.profile"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.profile.menu")),
            1
        )
        // Кнопка для создания кошелька
        val openWallet = systemGUI.createItem(
            Material.GREEN_WOOL,
            localizationManager.getMessage("localisation.inventory.item.open-wallet"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.open-wallet.menu", "amount" to "[todo AMOUNT]")),
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
            localizationManager.getMessage("localisation.inventory.item.info"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wait-banker.menu")),
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
            localizationManager.getMessage("localisation.inventory.item.profile"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.profile.menu", "amount" to "[todo AMOUNT]")),
            1
        )
        val closeWallet = systemGUI.createItem(
            Material.RED_WOOL,
            localizationManager.getMessage("localisation.inventory.item.close-wallet"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.close-wallet.menu")),
            1
        )
        // Кнопка для гайда
        val guid = systemGUI.createItem(
            Material.BOOKSHELF,
            localizationManager.getMessage("localisation.inventory.item.guid-book"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.guid-book.menu")),
            1
        )
        // Кнопка для Репорта
        val report = systemGUI.createItem(
            Material.FIRE_CHARGE,
            localizationManager.getMessage("localisation.inventory.item.report"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.report.menu")),
            1
        )
        //Кнопка связи с банкиром
        val sendMessageBanker = systemGUI.createItem(
            Material.WRITABLE_BOOK,
            localizationManager.getMessage("localisation.inventory.item.send-banker-message"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.send-banker-message.menu")),
            1
        )
        //Кнопка для действий с кошельком
        val actionsWallet = systemGUI.createItem(
            Material.PURPLE_WOOL,
            localizationManager.getMessage("localisation.inventory.item.actions"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.actions.menu")),
            2
        )
        // Кнопка для просмотра истории транзакций
        val history = systemGUI.createItem(
            Material.PAPER,
            "История транзакций",
            listOf("{Заполнить | History}"),
            5
        )
//        //Кнопка пополнения
//        val replenish = systemGUI.createItem(
//            Material.PURPLE_WOOL,
//            localizationManager.getMessage("localisation.inventory.item.replenish"),
//            listOf(localizationManager.getMessage("localisation.inventory.lore.replenish.menu")),
//            1
//        )
//        //Кнопка снятия
//        val takeOff = systemGUI.createItem(
//            Material.BLUE_WOOL,
//            localizationManager.getMessage("localisation.inventory.item.take-off"),
//            listOf(localizationManager.getMessage("localisation.inventory.lore.take-off.menu")),
//            1
//        )
        //Кнопка перевода
        val transfer = systemGUI.createItem(
            Material.CYAN_WOOL,
            localizationManager.getMessage("localisation.inventory.item.transfer"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.transfer.menu")),
            1
        )


        //Список штрафов DEV
        val fineList = systemGUI.createItem(
            Material.PINK_GLAZED_TERRACOTTA,
            localizationManager.getMessage("localisation.inventory.item.fine-list"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.fine-list.menu")),
            1
        )
        //Оплатить штраф DEV
        val payFine = systemGUI.createItem(
            Material.GREEN_GLAZED_TERRACOTTA,
            localizationManager.getMessage("localisation.inventory.item.pay-fine"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.appeal-fine.menu")),
            1
        )
        //Обажаловать штраф DEV
        val appealFine = systemGUI.createItem(
            Material.RED_GLAZED_TERRACOTTA,
            localizationManager.getMessage("localisation.inventory.item.appeal-fine"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.appeal-fine.menu")),
            1
        )

        //Список  счетов DEV
        val billList = systemGUI.createItem(
            Material.ORANGE_GLAZED_TERRACOTTA,
            localizationManager.getMessage("localisation.inventory.item.bill-list"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.bill-list.menu")),
            1
        )
        //Оплатить счет DEV
        val payBill = systemGUI.createItem(
            Material.MAGENTA_GLAZED_TERRACOTTA,
            localizationManager.getMessage("localisation.inventory.item.pay-bill"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.pay-bill.menu")),
            1
        )
        //Выставить счет DEV
        val putBill = systemGUI.createItem(
            Material.YELLOW_GLAZED_TERRACOTTA,
            localizationManager.getMessage("localisation.inventory.item.put-bill"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.put-bill.menu")),
            1
        )

        inventory.setItem(10, profile)
        inventory.setItem(16, closeWallet)

//        todo: Включить когда будет работать
//        inventory.setItem(39, guid)
//        inventory.setItem(40, report)
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
            localizationManager.getMessage("localisation.inventory.item.profile"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.profile.menu")),
            1
        )
        // Информация ожидания
        val waitBanker = systemGUI.createItem(
            Material.YELLOW_WOOL,
            localizationManager.getMessage("localisation.inventory.item.wait-banker"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wait-banker.menu")),
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
            localizationManager.getMessage("localisation.inventory.item.info"),
            listOf(localizationManager.getMessage("localisation.inventory.lore.wait-banker.menu")),
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