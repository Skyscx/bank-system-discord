package data

import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class TransferDataManager {
    private val transferData = ConcurrentHashMap<Player, TransferData>()

    fun setTargetPlayer(sender: Player, targetPlayerName: String) {
        transferData[sender] = TransferData(sender, targetPlayerName, 0)
    }

    fun setAmount(sender: Player, amount: Int) {
        val data = transferData[sender] ?: return
        transferData[sender] = data.copy(amount = amount)
    }

    fun getTransferData(sender: Player): TransferData? {
        return transferData[sender]
    }

    fun removeTransferData(sender: Player) {
        transferData.remove(sender)
    }

    data class TransferData(val sender: Player, val targetPlayerName: String, val amount: Int)
    companion object {
        val instance = TransferDataManager()
    }
}