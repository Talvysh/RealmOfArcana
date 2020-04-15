package com.realmofarcana.region

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import com.realmofarcana.region.Region.Companion.RegionType
import org.bukkit.entity.Bee
import org.bukkit.event.entity.EntityDeathEvent

class RegionEvents: Listener {
    // Cancel breaking blocks in certain situations.
    @EventHandler fun onBlockBreak(e: BlockBreakEvent) {
        // Triggering player must have a Member reference.
        val member = Member.fromID(e.player.uniqueId.toString()) ?: return
        // Get Land reference that contains the triggering block.
        val land = Region.fromChunk(e.block.chunk) ?: return

        // If the member has build permissions: Exit.
        if (land.canBuild(member)) return

        e.isCancelled = true
    }

    // Cancel placing blocks in certain situations.
    @EventHandler fun onBlockPlace(e: BlockPlaceEvent) {
        val member = Member.fromID(e.player.uniqueId.toString()) ?: return
        val land = Region.fromChunk(e.blockAgainst.chunk) ?: return

        if (!land.canBuild(member)) {
            e.isCancelled = true
        }
    }

    // Cancel certain actions involving blocks in the world.
    @EventHandler fun onInteract(e: PlayerInteractEvent) {
        // No block?  Exit.
        if (e.clickedBlock == null) return
        // Triggering player must have a Member reference.
        val member = Member.fromID(e.player.uniqueId.toString()) ?: return
        // Get Land reference that contains the triggering block.
        val land = Region.fromChunk(e.clickedBlock!!.chunk) ?: return

        // Do they have basic permission to build/destroy here?
        if (land.canBuild(member)) return

        // Check for blacklisted blocks that can't be interacted with.
        if (Region.blacklist.contains(e.clickedBlock?.type)) {
            e.isCancelled = true // Cancel the interaction.
        }
    }

    // Let the player know what land they're within.
    @EventHandler fun onPlayerMove(e: PlayerMoveEvent) {
        val member = Member.fromID(e.player.uniqueId.toString()) ?: return
        val region = Region.fromChunk(e.player.location.chunk)

        if (member.whereAmI == region) return

        member.whereAmI = region

        member.landTitle.setTitle(region?.getTitle() ?: "${ChatColor.GREEN}Wilderness")
    }

    // Protect players in Sanctums
    @EventHandler fun onPlayerDamage (e: EntityDamageEvent) {

        if (e.entity is Player) {
            val member = Member.fromID(e.entity.uniqueId.toString())
            val rg = Region.fromChunk(e.entity.location.chunk)
            
            if (rg == null)
                return
            else if (rg.type == RegionType.SANCTUM)
                e.isCancelled = true
        }
        else if (e.entity is Bee) {
            e.isCancelled = true
        }
    }

    // Double xp
    @EventHandler fun onXpDrop (e: EntityDeathEvent) {
        e.droppedExp = e.droppedExp * 3
    }
}