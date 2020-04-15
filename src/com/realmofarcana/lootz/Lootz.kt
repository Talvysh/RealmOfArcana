package com.realmofarcana.lootz

import com.realmofarcana.Helper
import com.realmofarcana.chat.Chat
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentWrapper
import org.bukkit.inventory.ItemStack
import java.io.File

class Lootz (val id: String) {
    var display = ""
    var lore = ""
    var mat: Material
    var count: IntRange
    var score: Int
    val enchants = mutableMapOf<Enchantment, IntRange>()

    fun drop () : ItemStack {
        val itemStack = ItemStack(mat, count.random())

        // Set META
        val meta = itemStack.itemMeta!!

        if (display != "")
            meta.setDisplayName(Chat.formatString(display))
        if (lore != "")
            meta.lore = mutableListOf(lore)

        itemStack.itemMeta = meta

        // Set enchantments
        enchants.forEach {
            itemStack.addUnsafeEnchantment(it.key, it.value.random())
        }

        return itemStack
    }

    init {
        display = config.getString("$id.display") ?: ""
        mat = Material.valueOf(config.getString("$id.mat") ?: "BARRIER")
        score = config.getInt("$id.score")
        count = 0..1

        // Get count range
        val array = config.getIntegerList("$id.count")
        when (array.size) {
            1 -> count = array[0]..array[0]
            2 -> count = array[0]..array[1]
        }

        // Get enchantments
        val enchantmentSection = config.getConfigurationSection("$id.enchantments")
        enchantmentSection?.getKeys(false)?.forEach {
            val ench = EnchantmentWrapper(it).enchantment
            println("Enchantment: ${ench.key}")
        }
    }

    companion object {
        val instances = mutableListOf<Lootz>()
        private lateinit var file: File
        private lateinit var config: YamlConfiguration

        fun fromID (id: String) : Lootz? {
            instances.forEach {
                if (it.id == id)
                    return it
            }
            return null
        }

        fun reload () {
            instances.clear()
            init()
        }

        fun init () {
            file = Helper.setupFile("lootz")
            config = Helper.setupConfig(file)

            config.getKeys(false).forEach {
                Lootz(it)
            }
        }
    }
}