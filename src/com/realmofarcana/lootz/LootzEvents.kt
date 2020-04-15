package com.realmofarcana.lootz

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType

class LootzEvents : Listener {
    @EventHandler fun onChestOpen (e: InventoryOpenEvent) {
        if (validInventoryType(e.view.topInventory.type)) {
            LootzContainer.instances.forEach {
                if (it.location == e.view.topInventory.location) {
                    e.isCancelled = true
                    e.player.openInventory(it.chest)
                }
            }
        }
    }

    private fun validInventoryType (t: InventoryType) : Boolean {
        return when (t) {
            InventoryType.CHEST -> true
            InventoryType.BARREL -> true
            InventoryType.SHULKER_BOX -> true
            else -> false
        }
    }
}