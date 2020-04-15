package com.realmofarcana.world

import com.realmofarcana.chat.Chat
import com.realmofarcana.ROA
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle

object World {
    var theCulling = false
    var cullingTitle = ROA.instance.server.createBossBar("The Culling", BarColor.RED, BarStyle.SOLID)

    fun start() {
        cullingTitle.isVisible = false
        cullingTitle.addFlag(BarFlag.CREATE_FOG)
        cullingTitle.addFlag(BarFlag.DARKEN_SKY)
    }

    fun startCulling() {
        theCulling = true
        cullingTitle.isVisible = true

        ROA.instance.server.onlinePlayers.forEach {
            it.playSound(it.location, Sound.ENTITY_ZOMBIE_PIGMAN_ANGRY, 1.0f, 0.1f)
            it.playSound(it.location, Sound.ENTITY_ZOMBIE_PIGMAN_ANGRY, 1.0f, 0.1f)
            it.playSound(it.location, Sound.ENTITY_ZOMBIE_PIGMAN_ANGRY, 1.0f, 0.1f)
        }

        Chat.event("§cThe Culling§f has begun!  Death & destruction begins, all land protection is turned off.")
    }

    fun endCulling() {
        theCulling = false
        cullingTitle.isVisible = false

        ROA.instance.server.onlinePlayers.forEach {
            it.playSound(it.location, Sound.ENTITY_SHEEP_AMBIENT, 1.0f, 1.0f)
            it.playSound(it.location, Sound.ENTITY_SHEEP_AMBIENT, 1.0f, 1.0f)
            it.playSound(it.location, Sound.ENTITY_SHEEP_AMBIENT, 1.0f, 1.0f)
        }

        Chat.event("§cThe Culling§f has ended.  Go back to your §9boring§f lives, and §9rebuild§f.  The time will come again to §chunt§f.")
    }
}
