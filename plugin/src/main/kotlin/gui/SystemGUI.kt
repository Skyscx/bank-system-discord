package gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SystemGUI {
    fun createItem(
        material: Material,
        name: String? = null,
        lore: List<String> = emptyList(),
        customModelData: Int? = null,
        italic: Boolean = false,
        bold: Boolean = false,
        underlined: Boolean = false,
        strikethrough: Boolean = false,
        obfuscated: Boolean = false
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        // Установка уникального названия с различными стилями текста
        val nameComponent = if (name.isNullOrBlank()) {
            Component.text(material.name)
        } else {
            Component.text(name)
                .decoration(TextDecoration.ITALIC, italic)
                .decoration(TextDecoration.BOLD, bold)
                .decoration(TextDecoration.UNDERLINED, underlined)
                .decoration(TextDecoration.STRIKETHROUGH, strikethrough)
                .decoration(TextDecoration.OBFUSCATED, obfuscated)
        }

        meta?.displayName(nameComponent)

        // Установка уникального описания (lore) с различными стилями текста
        val loreComponents = lore.flatMap { line ->
            line.split("\n").map {
                Component.text(it)
                    .decoration(TextDecoration.ITALIC, italic)
                    .decoration(TextDecoration.BOLD, bold)
                    .decoration(TextDecoration.UNDERLINED, underlined)
                    .decoration(TextDecoration.STRIKETHROUGH, strikethrough)
                    .decoration(TextDecoration.OBFUSCATED, obfuscated)
            }
        }
        meta?.lore(loreComponents)

        if (customModelData != null) {
            meta?.setCustomModelData(customModelData)
        }

        item.setItemMeta(meta)
        return item
    }
}