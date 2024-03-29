package com.realmofarcana

import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Helper {
    fun readTextFile(fileName: String): String {
        var s = ""
        File("${ROA.instance.dataFolder}/$fileName.txt").forEachLine {
            s += it + "\n"
            if (it.isEmpty()) s += "\n"
        }
        return s + "\n"
    }

    fun isName(s: String): Boolean {
        return (Regex("^[a-zA-Z]+$").containsMatchIn(s))
    }

    fun hasSpecials(s: String): Boolean {
        return (Regex("[!@#\$%^&*(),.?\":{}|<>]").containsMatchIn(s))
    }

    inline fun <reified T : Enum<T>> toEnumOrNull(name: String): T? {
        return enumValues<T>().find { it.name == name }
    }

    // QOL function
    fun setupFile (fileName: String) : File {
        val file = File(ROA.instance.dataFolder, "$fileName.yml")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            ROA.instance.saveResource("$fileName.yml", false)
        }
        return file
    }

    // QOL function
    fun setupConfig (file: File): YamlConfiguration {
        val config = YamlConfiguration()
        config.load(file)
        return config
    }

    fun updateSignColors(sign: Sign) {
        // Change any & character with § to format colors on signs
        for (i in 0..sign.lines.size) {
            var line = sign.lines[i]
            line = ChatColor.translateAlternateColorCodes('&', line)
            sign.setLine(i, line)
        }
    }
}