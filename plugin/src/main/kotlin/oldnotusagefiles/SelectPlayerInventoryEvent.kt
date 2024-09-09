//package oldnotusagefiles
//
//import App.Companion.localizationManager
//import functions.Functions
//import gui.InventoryManager
//import io.papermc.paper.event.player.AsyncChatEvent
//import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
//import org.bukkit.Material
//import org.bukkit.entity.Player
//import org.bukkit.event.EventHandler
//import org.bukkit.event.Listener
//import org.bukkit.event.inventory.InventoryClickEvent
//import org.bukkit.event.inventory.InventoryType
//
//class SelectPlayerInventoryEvent(private val amountPlayerInventoryFactory: (String) -> AmountPlayerInventory) : Listener {
//    private val functions = Functions()
//    private val inventoryManager = InventoryManager()
//    private val playersWaitingForMessage = mutableMapOf<Player, String>()
//
//    @EventHandler
//    fun onClick(e: InventoryClickEvent) {
//        val player = e.whoClicked as Player
//        if (e.view.type == InventoryType.CHEST) {
//            val title = e.view.title()
//            val expectedTitle = localizationManager.getMessage("localisation.inventory.title.select-player-transfer")
//            if (functions.isComponentEqual(title, expectedTitle)) {
//                val currentItem = e.currentItem ?: return
//                val itemMeta = currentItem.itemMeta ?: return
//                if (itemMeta.hasDisplayName()) {
//                    e.isCancelled = true
//                    player.closeInventory()
//                    val displayNameComponent = itemMeta.displayName() ?: return
//                    val titleOtherPlayer = localizationManager.getMessage("localisation.inventory.other-player")
//                    if (currentItem.type == Material.PLAYER_HEAD) {
//                        // Кликнули по голове игрока
//                        val targetPlayerName = itemMeta.displayName() ?: return
//                        val textTargetPlayerName = PlainTextComponentSerializer.plainText().serialize(targetPlayerName)
//                        val amountInventory = amountPlayerInventoryFactory(textTargetPlayerName).createInventory(player)
//                        player.sendMessage("Opening inventory for $textTargetPlayerName") // Отладочное сообщение
//                        player.openInventory(amountInventory)
//                    }
//                }
//            }
//        }
//    }
//
//    @EventHandler
//    fun onPlayerChat(e: AsyncChatEvent) {
//        val player = e.player
//        if (playersWaitingForMessage.containsKey(player)) {
//            e.isCancelled = true // Отменяем отправку сообщения в чат
//            val type = playersWaitingForMessage.remove(player) ?: return
//            val message = PlainTextComponentSerializer.plainText().serialize(e.message())
//            if (type == "other") {
//                val amountInventory = amountPlayerInventoryFactory(message).createInventory(player)
//                player.openInventory(amountInventory)
//            }
//        }
//    }
//}
