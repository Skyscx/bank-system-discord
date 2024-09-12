package gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

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

    fun createPlayerHead(player: Player, lore: String): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val meta = item.itemMeta as SkullMeta
        meta.owningPlayer = player

        // Установка уникального названия с различными стилями текста
        val nameComponent = Component.text(player.name)
            .decoration(TextDecoration.BOLD, true)

        meta.displayName(nameComponent)

        // Установка уникального описания (lore) с различными стилями текста
        val loreComponents = listOf(
            Component.text(lore)
        )
        meta.lore(loreComponents)

        item.setItemMeta(meta)
        return item
    }
    fun createSkull(uuid: UUID, name: String, lore: List<String>, amount: Int): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, amount)
        val meta = item.itemMeta as SkullMeta
        val player = Bukkit.getOfflinePlayer(uuid)
        meta.owningPlayer = player

        // Установка уникального названия с различными стилями текста
        val nameComponent = Component.text(name)
            .decoration(TextDecoration.BOLD, true)

        meta.displayName(nameComponent)

        // Установка уникального описания (lore) с различными стилями текста
        val loreComponents = lore.map { Component.text(it) }
        meta.lore(loreComponents)

        item.setItemMeta(meta)
        return item
    }

}