package data.managers

import org.bukkit.entity.Player

class ConvertDataManager private constructor() {

    companion object {
        val instance = ConvertDataManager()
    }

    private val convertDataMap = mutableMapOf<Player, ConvertData>()

    fun setPlayer(player: Player) {
        val transferData = convertDataMap.getOrPut(player) { ConvertData() }
    }
    fun setAmount(player: Player, amount: Int) {
        val transferData = convertDataMap.getOrPut(player) { ConvertData() }
        transferData.amount = amount
    }

    fun setType(player: Player, type: String) {
        val transferData = convertDataMap.getOrPut(player) { ConvertData() }
        transferData.type = type
    }

    fun getConvertData(player: Player): ConvertData? {
        return convertDataMap[player]
    }

    fun removeConvertData(player: Player) {
        convertDataMap.remove(player)
    }
}

data class ConvertData(
    var amount: Int = 0,
    var type: String? = "DIAMOND_ORE"
)
