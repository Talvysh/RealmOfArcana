package com.realmofarcana.member

import com.realmofarcana.ROA
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class Rank (val name: String) {
    var tag = ""
    var perms = mutableListOf<String>()
    var maxChunks = 9
    var etherealCooldown = 600 // 10 mins

    init {
        tag              = config.getString("$name.tag") ?: ""
        etherealCooldown = config.getInt("$name.ethereal-cooldown")
        maxChunks        = config.getInt("$name.max-chunks")
        perms            = config.getStringList("$name.perms")

        var child = config.getString("$name.inherit")
        while (!child.isNullOrBlank()) {
            perms.addAll(config.getStringList("$child.perms"))
            child = config.getString("$child.inherit")
        }

        instances.add(this)
    }

    companion object {
        val instances = mutableListOf<Rank>()
        private val file = File("${ROA.instance.dataFolder}/ranks.yml")
        private val config = YamlConfiguration.loadConfiguration(file)
        lateinit var default: Rank

        fun init() {
            if (!file.exists())
                config.save(file)

            println("[ LOADING RANKS ]")
            config.getKeys(false).forEach {
                Rank(it)
            }

            default = getByName("Member")
        }

        fun getByName(_name: String): Rank {
            instances.forEach {
                if (it.name.equals(_name, true))
                    return it
            }
            // Couldn't find the rank, return the default rank.
            return default
        }
    }
}