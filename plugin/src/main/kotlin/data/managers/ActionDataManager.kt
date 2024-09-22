package data.managers

import org.bukkit.entity.Player

class ActionDataManager private constructor() {

    companion object {
        val instance = ActionDataManager()
    }

    private val actionDataMap = mutableMapOf<Player, ActionData>()

    fun setPlayer(player: Player, amount: Int) {
        val transferData = actionDataMap.getOrPut(player) { ActionData() }
        transferData.amount = amount
    }

    fun setAmount(player: Player, amount: Int) {
        val transferData = actionDataMap.getOrPut(player) { ActionData() }
        transferData.amount = amount
    }

    fun getActionData(player: Player): ActionData? {
        return actionDataMap[player]
    }

    fun removeActionData(player: Player) {
        actionDataMap.remove(player)
    }
}

data class ActionData(
    var amount: Int = 0,
)
