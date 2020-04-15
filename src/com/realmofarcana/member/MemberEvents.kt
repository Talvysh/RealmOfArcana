package com.realmofarcana.member

import com.realmofarcana.chat.Chat
import com.realmofarcana.ROA
import com.realmofarcana.world.World
import com.realmofarcana.region.Region
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

class MemberEvents: Listener {
    @EventHandler fun onPlayerJoin(e: PlayerJoinEvent) {
        var member = Member.fromID(e.player.uniqueId.toString())

        if (member == null) {
            member = Member(e.player)

            // Give starting items:
            e.player.inventory.addItem(
                ItemStack(Material.IRON_SWORD),
                ItemStack(Material.SHIELD),
                ItemStack(Material.IRON_PICKAXE),
                ItemStack(Material.IRON_SHOVEL),
                ItemStack(Material.IRON_AXE),
                ItemStack(Material.BREAD, 16),
                ItemStack(Material.WHEAT_SEEDS, 16)
            )

        }

        member.player = e.player
        member.setPerms()

        Chat.raw(e.player, Chat.formatString(ROA.instance.config.getString("motd")!!))
        if (World.theCulling) World.cullingTitle.addPlayer(e.player)

        member.whereAmI = Region.fromChunk(e.player.location.chunk)
        member.landTitle.setTitle(member.whereAmI?.getTitle() ?: "${ChatColor.GREEN}Wilderness")
        member.landTitle.progress = 0.0
        member.landTitle.addPlayer(e.player)

        // Add players to World boss bars.
        if (!World.cullingTitle.players.contains(e.player)) {
            World.cullingTitle.addPlayer(e.player)
        }
    }

    /*==/ When Player quits the server. /==*/
    @EventHandler fun onPlayerQuit(e: PlayerQuitEvent) {
        val member = Member.fromID(e.player.uniqueId.toString())!!
        member.removePerms()
        member.player = null
    }

    /*==/ When Player sends a chat message. /==*/
    @EventHandler fun onPlayerChat(e: AsyncPlayerChatEvent) {
        val member = Member.fromID(e.player.uniqueId.toString())

        if (member == null) {
            e.isCancelled = true
            return
        }

        var message = "${member.rank.tag} "

        if (member.clan != null)
            message += "<${ChatColor.YELLOW}${member.clan!!.name}${ChatColor.WHITE}> "

        message += "${ChatColor.WHITE}${member.username}${ChatColor.AQUA}:${ChatColor.WHITE} ${e.message}"

        if (member.hasPerm("roa.format")) {
            message = Chat.formatString(message)
        }

        Bukkit.broadcastMessage(message)
        e.isCancelled = true
    }

    @EventHandler fun onSignFormat (e: SignChangeEvent) {
        for (i in 0..3) {
            e.setLine(i, Chat.formatString(e.getLine(i) ?: ""))
        }
    }

    @EventHandler fun onCreativeAttack (e: EntityDamageByEntityEvent) {
        if (e.damager is Player) {
            val player = e.damager as Player
            if (player.gameMode == GameMode.CREATIVE)
                e.entity.remove()
        }
    }
}