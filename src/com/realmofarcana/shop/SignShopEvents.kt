package com.realmofarcana.shop

import com.realmofarcana.Helper
import com.realmofarcana.chat.Chat
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

class SignShopEvents: Listener {
    @EventHandler fun onSignChange(e: SignChangeEvent) {
        if (e.lines.isEmpty()) return

        if (e.lines[0].contains("sell")) {
            e.lines[0] = "§aSELL"
            e.lines[1] = "10"
            e.lines[2] = "EMERALD"
            e.lines[3] = "30C"
        }

        var sign = e.block.state as Sign

        // Check sign lines for errors
        var amount = e.lines[1].toIntOrNull()
        var material = Helper.toEnumOrNull<Material>(e.lines[2])
        var cost = e.lines[3].toIntOrNull()

        if (amount == null || cost == null) {
            Chat.error(e.player, "lines 2 and 4 must be integer values.")
            return
        } else if (material == null) {
            Chat.error(e.player, "Line 3 must be a valid material.")
            return
        }

        sign.isGlowingText = true
        Helper.updateSignColors(sign)
        SignShop(sign, amount, material, cost)
    }

    @EventHandler fun onSignInteract(e: PlayerInteractEvent) {
        if (e.clickedBlock?.blockData == null) return

        var blockData = e.clickedBlock!!.blockData

        if (!blockData.material.name.contains("SIGN")) return

        // must use the state of a block to cast to another block
        var sign = e.clickedBlock!!.state as Sign

        var shop = SignShop.getShop(e.clickedBlock as Block)
        shop?.openInventory(e.player)
    }
}