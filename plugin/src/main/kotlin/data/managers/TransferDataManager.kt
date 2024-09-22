package data.managers

import org.bukkit.entity.Player

class TransferDataManager private constructor() {

    companion object {
        val instance = TransferDataManager()
    }

    private val transferDataMap = mutableMapOf<Player, TransferData>()

    fun setTargetPlayer(player: Player, targetPlayerName: String) {
        val transferData = transferDataMap.getOrPut(player) { TransferData() }
        transferData.targetPlayerName = targetPlayerName
    }

    fun setAmount(player: Player, amount: Int) {
        val transferData = transferDataMap.getOrPut(player) { TransferData() }
        transferData.amount = amount
    }

    fun setComment(player: Player, comment: String) {
        val transferData = transferDataMap.getOrPut(player) { TransferData() }
        transferData.comment = comment
    }

    fun getTransferData(player: Player): TransferData? {
        return transferDataMap[player]
    }

    fun removeTransferData(player: Player) {
        transferDataMap.remove(player)
    }
}

data class TransferData(
    var targetPlayerName: String = "",
    var amount: Int = 0,
    var comment: String? = null
)
