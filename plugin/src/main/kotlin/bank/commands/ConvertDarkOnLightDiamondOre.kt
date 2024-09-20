package bank.commands

import App.Companion.localized
import functions.Functions
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ConvertDarkOnLightDiamondOre : CommandExecutor {
    private val functions = Functions()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val isPlayer = functions.senderIsPlayer(sender)
        if (!isPlayer.second) {
            sender.sendMessage(isPlayer.first)
            return true
        }

        val player = sender as Player

        if (args.isEmpty()) {
            //TODO: Сделать открытие меню инвентаря со всеми доступными функциями.
            sender.sendMessage("localisation.messages.out.developing".localized())
            return true
        }

        when (args[0].lowercase()) {
            "all" -> {
                // Логика для обработки аргумента "all"
                convertAllDarkToLight(player)
            }
            else -> {
                // Проверка, является ли аргумент числом
                val amount = args[0].toIntOrNull()
                if (amount != null) {
                    // Логика для обработки числового аргумента
                    convertSpecificAmountDarkToLight(player, amount)
                } else {
                    sender.sendMessage("localisation.messages.out.invalid-argument".localized())
                }
            }
        }

        return true
    }

    private fun convertAllDarkToLight(player: Player) {
        val inventory = player.inventory
        val darkDiamondOre = Material.DEEPSLATE_DIAMOND_ORE // Замените на фактический материал dark diamond ore
        val lightDiamondOre = Material.DIAMOND_ORE // Замените на фактический материал light diamond ore

        for (item in inventory.contents) {
            if (item != null && item.type == darkDiamondOre) {
                item.type = lightDiamondOre
            }
        }

        player.sendMessage("localisation.messages.out.converted-all".localized())
    }

    private fun convertSpecificAmountDarkToLight(player: Player, amount: Int) {
        val inventory = player.inventory
        val darkDiamondOre = Material.DEEPSLATE_DIAMOND_ORE // Замените на фактический материал dark diamond ore
        val lightDiamondOre = Material.DIAMOND_ORE // Замените на фактический материал light diamond ore

        var convertedAmount = 0

        for (item in inventory.contents) {
            if (item != null && item.type == darkDiamondOre) {
                val amountToConvert = minOf(amount - convertedAmount, item.amount)
                item.amount -= amountToConvert
                convertedAmount += amountToConvert

                if (item.amount == 0) {
                    item.type = lightDiamondOre
                    item.amount = amountToConvert
                } else {
                    val newItem = ItemStack(lightDiamondOre, amountToConvert)
                    inventory.addItem(newItem)
                }

                if (convertedAmount >= amount) {
                    break
                }
            }
        }

        player.sendMessage("localisation.messages.out.converted-specific".localized( "amount" to convertedAmount.toString()))
    }
}