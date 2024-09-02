package gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SystemGUI {
    fun createItem(material: Material, name: String, lore: List<String> = emptyList(), customModelData: Int? = null): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.displayName(Component.text(name))
        meta?.lore(lore.map { Component.text(it) }) // Установите цвет текста по вашему усмотрению
        if (customModelData != null) {
            meta?.setCustomModelData(customModelData)
        }
        item.setItemMeta(meta)
        return item
    }
}