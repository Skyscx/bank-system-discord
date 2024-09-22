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

        if (args.size < 2) {
            sender.sendMessage("localisation.messages.out.developing".localized())
            return true
        }

        val material = args[0].uppercase()
        val amountOrAll = args[1].lowercase()

        when (material) {
            "DEEPSLATE_DIAMOND_ORE" -> {
                when (amountOrAll) {
                    "all" -> convertAll(player, Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND_ORE)
                    else -> {
                        val amount = amountOrAll.toIntOrNull()
                        if (amount != null) {
                            convertSpecificAmount(player, Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND_ORE, amount)
                        } else {
                            sender.sendMessage("localisation.messages.usage.convert".localized())
                        }
                    }
                }
            }
            "DIAMOND_ORE" -> {
                when (amountOrAll) {
                    "all" -> convertAll(player, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)
                    else -> {
                        val amount = amountOrAll.toIntOrNull()
                        if (amount != null) {
                            convertSpecificAmount(player, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, amount)
                        } else {
                            sender.sendMessage("localisation.messages.usage.convert".localized())
                        }
                    }
                }
            }
            else -> {
                sender.sendMessage("localisation.block.type.unknown".localized())
            }
        }

        return true
    }

    private fun convertAll(player: Player, fromMaterial: Material, toMaterial: Material) {
        val inventory = player.inventory

        for (item in inventory.contents) {
            if (item != null && item.type == fromMaterial) {
                item.type = toMaterial
            }
        }

        player.sendMessage("localisation.messages.out.converted-all".localized())
    }

    private fun convertSpecificAmount(player: Player, fromMaterial: Material, toMaterial: Material, amount: Int) {
        val inventory = player.inventory
        var convertedAmount = 0

        for (item in inventory.contents) {
            if (item != null && item.type == fromMaterial) {
                val amountToConvert = minOf(amount - convertedAmount, item.amount)
                item.amount -= amountToConvert
                convertedAmount += amountToConvert

                if (item.amount == 0) {
                    item.type = toMaterial
                    item.amount = amountToConvert
                } else {
                    val newItem = ItemStack(toMaterial, amountToConvert)
                    inventory.addItem(newItem)
                }

                if (convertedAmount >= amount) {
                    break
                }
            }
        }

        player.sendMessage("localisation.messages.out.converted-specific".localized("amount" to convertedAmount.toString()))
    }
}