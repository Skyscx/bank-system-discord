//package oldnotusagefiles
//
//import App.Companion.localizationManager
//import functions.Functions
//import gui.InventoryCreator
//import gui.SystemGUI
//import net.kyori.adventure.text.Component
//import org.bukkit.Bukkit
//import org.bukkit.Material
//import org.bukkit.entity.Player
//import org.bukkit.event.EventHandler
//import org.bukkit.event.Listener
//import org.bukkit.event.inventory.InventoryClickEvent
//import org.bukkit.event.inventory.InventoryType
//import org.bukkit.inventory.Inventory
//import org.bukkit.inventory.ItemStack
//
//class SelectPlayerInventory : InventoryCreator, Listener {
//    private val systemGUI = SystemGUI()
//    private val functions = Functions()
//
//    // Массив с именами игроков
////    private val playerNames = listOf(
////        "Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "Player7", "Player8", "Player9", "Player10",
////        "Player11", "Player12", "Player13", "Player14", "Player15", "Player16", "Player17", "Player18", "Player19", "Player20",
////        "Player21", "Player22", "Player23", "Player24", "Player25", "Player26", "Player27", "Player28", "Player29", "Player30",
////        "Player31", "Player32", "Player33", "Player34", "Player35", "Player36", "Player37", "Player38", "Player39", "Player40",
////        "Player41", "Player42", "Player43", "Player44", "Player45", "Player46", "Player47", "Player48", "Player49", "Player50",
////        "Player51", "Player52", "Player53", "Player54", "Player55", "Player56", "Player57", "Player58", "Player59", "Player60",
////        "Player61", "Player62", "Player63", "Player64", "Player65", "Player66", "Player67", "Player68", "Player69", "Player70",
////        "Player71", "Player72", "Player73", "Player74", "Player75", "Player76", "Player77", "Player78", "Player79", "Player80",
////        "Player81", "Player82", "Player83", "Player84", "Player85", "Player86", "Player87", "Player88", "Player89", "Player90",
////        "Player91", "Player92", "Player93", "Player94", "Player95", "Player96", "Player97", "Player98", "Player99", "Player100"
////    )
//
//    // Хранилище текущей страницы для каждого игрока
//    private val playerPages = mutableMapOf<Player, Int>()
//
//    override fun createInventory(player: Player): Inventory {
//        val title = Component.text(localizationManager.getMessage("localisation.inventory.title.select-player-transfer"))
//        val inventory = Bukkit.createInventory(null, 54, title)
//
//        val currentPage = playerPages.getOrDefault(player, 0)
//        val pageSize = 51 // 53 slots for items, 1 slot for "next page" button
//
//        val onlinePlayers = Bukkit.getOnlinePlayers().toList().filter { it != player }
//
//
//        val startIndex = currentPage * pageSize
//        val endIndex = (startIndex + pageSize).coerceAtMost(onlinePlayers.size)
//
//        for (i in startIndex..<endIndex) {
//            val playerHead = systemGUI.createPlayerHead(onlinePlayers[i], "Выбрать")
//            inventory.setItem(i - startIndex + 1, playerHead) // Start from slot 1 to leave slot 0 for "Other Player" item
//        }
//
//        // Add "Other Player" item in slot 0
//        val otherPlayerItem = createOtherPlayerItem()
//        inventory.setItem(0, otherPlayerItem)
//
//        // Add "Next Page" button in slot 53 if there are more players
//        if (endIndex < onlinePlayers.size) {
//            val nextPageItem = createNextPageItem()
//            inventory.setItem(53, nextPageItem)
//        }
//
//        // Add "Previous Page" button in slot 52 if the current page is not the first page
//        if (currentPage > 0) {
//            val previousPageItem = createPreviousPageItem()
//            inventory.setItem(52, previousPageItem)
//        }
//
//        return inventory
//    }
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
//                    val titleNextPage = "Next Page"
//                    val titlePreviousPage = "Previous Page"
//
//                    if (functions.isComponentEqual(displayNameComponent, titleNextPage)) {
//                        playerPages[player] = playerPages.getOrDefault(player, 0) + 1
//                        player.openInventory(createInventory(player))
//                    } else if (functions.isComponentEqual(displayNameComponent, titlePreviousPage)) {
//                        playerPages[player] = playerPages.getOrDefault(player, 0) - 1
//                        player.openInventory(createInventory(player))
//                    }
//                    return
//                }
//            }
//        }
//    }
//    private fun createOtherPlayerItem(): ItemStack {
//        return systemGUI.createItem(
//            material = Material.BARRIER,
//            name = localizationManager.getMessage("localisation.inventory.other-player"),
//            lore = listOf("Click to select another player"),
//            customModelData = null,
//            italic = false,
//            bold = true,
//            underlined = false,
//            strikethrough = false,
//            obfuscated = false
//        )
//    }
//
//    private fun createNextPageItem(): ItemStack {
//        return systemGUI.createItem(
//            material = Material.ARROW,
//            name = "Next Page",
//            lore = listOf("Click to go to the next page"),
//            customModelData = null,
//            italic = false,
//            bold = true,
//            underlined = false,
//            strikethrough = false,
//            obfuscated = false
//        )
//    }
//
//    private fun createPreviousPageItem(): ItemStack {
//        return systemGUI.createItem(
//            material = Material.ARROW,
//            name = "Previous Page",
//            lore = listOf("Click to go to the previous page"),
//            customModelData = null,
//            italic = false,
//            bold = true,
//            underlined = false,
//            strikethrough = false,
//            obfuscated = false
//        )
//    }
//}