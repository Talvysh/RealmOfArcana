package com.realmofarcana.shop

import com.realmofarcana.ROA
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

class Shop (val name: String) {
    var displayName = ""
    var location: Location
    var buying = Bukkit.createInventory(null, 1, "")
    var selling = Bukkit.createInventory(null, 1, "")

    init {
        displayName = config.getString("$name.displayName") ?: ""

        location = Location(
            ROA.instance.server.getWorld(config.getString("$name.World")!!),
            config.getDouble("x"),
            config.getDouble("y"),
            config.getDouble("z")
        )

        config.getConfigurationSection("$name.Buying")?.getKeys(false)?.forEach {
            var item = ItemStack(Material.valueOf(it))

        }
    }

    companion object {
        val instances = mutableListOf<Shop>()
        private val file = File("${ROA.instance.dataFolder}/shops.yml")
        private val config = YamlConfiguration.loadConfiguration(file)

        fun init() {
            if (!file.exists())
                config.save(file)

            println("[ Loading Shops ]")
            config.getKeys(false).forEach {
                Shop(it)
            }
        }
    }
}