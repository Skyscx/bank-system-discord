package gui

import data.managers.ActionDataManager
import data.managers.ConvertDataManager
import data.managers.TransferDataManager
import gui.convertdiamonds.ConvertDiamondsBlocksInventory
import gui.wallletmenu.WalletMenuInventory
import gui.wallletmenu.actionwallet.WalletActionsInventory
import gui.wallletmenu.actionwallet.WalletHistoryInventory
import gui.wallletmenu.closewallet.WalletCloseInventory
import gui.wallletmenu.openwallet.WalletOpenInventory
import gui.wallletmenu.reportwallet.WalletReportInventory
import gui.wallletmenu.transferwallet.SelectPlayerInventory
import oldnotusagefiles.removewallet.WalletRemoveInventory
import org.bukkit.entity.Player

class InventoryManager {

    private val inventoryCreators = mapOf(
        "open" to WalletOpenInventory(),
        "remove" to WalletRemoveInventory(), //todo: remove
        "menu" to WalletMenuInventory(),
        "close" to WalletCloseInventory(),
        "actions" to WalletActionsInventory(ActionDataManager.instance),
        "reports" to WalletReportInventory(),
        "selectPlayerForTransfer" to SelectPlayerInventory(TransferDataManager.instance),
        "history" to WalletHistoryInventory(),
        "convert" to ConvertDiamondsBlocksInventory(ConvertDataManager.instance)
        /**more inventory**/
    )

    fun openInventory(player: Player, inventoryType: String) {
        val creator = inventoryCreators[inventoryType]

        if (creator != null) {
            val inventory = creator.createInventory(player)
            player.openInventory(inventory)
        } else {
            player.sendMessage("Unknown inventory type: $inventoryType") //todo: переделать сообщение на конфиг месседж
        }
    }
    fun openInitialTransferInventory(player: Player) {
        val selectPlayerInventory = inventoryCreators["selectPlayerForTransfer"] as? SelectPlayerInventory
        selectPlayerInventory?.openInitialInventory(player)
    }
    fun openInitialHistoryInventory(player: Player) {
        val walletHistoryInventory = inventoryCreators["history"] as? WalletHistoryInventory
        walletHistoryInventory?.openInitialInventory(player)
    }
}
