package gui

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SystemGUI {
    fun createItem(material: Material, name: String, lore: List<String> = emptyList()): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta!!
        meta.setDisplayName(name)
        meta.lore = lore
        item.setItemMeta(meta)
        return item
    }
}