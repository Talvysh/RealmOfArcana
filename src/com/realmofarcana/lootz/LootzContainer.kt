package com.realmofarcana.lootz

import com.realmofarcana.chat.Chat
import com.realmofarcana.ROA
import com.realmofarcana.SQL
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.Container
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.sql.ResultSet
import java.util.*
import kotlin.concurrent.schedule
import kotlin.random.Random

class LootzContainer : InventoryHolder {
    var id = UUID.randomUUID().toString()
    private var lootTable: LootTable
    var location: Location
    private lateinit var markerLocation: Location
    lateinit var chest: Inventory
    private var particleTaskID = -1
    var looted = false // If it's been open before the next cycle

    fun refill () {
        //for (slot in 0 until inventory.size) {
        //    val roll = Random.nextInt(1, 100)
        //    var drop = LootItem(Material.AIR, 0..1, 50)
        //
        //    lootTable.items.forEach {
        //        // Get all drops with score lower than our roll
        //        if (it.score <= roll) {
        //            // Get the drop as high as possible
        //            if (drop.score < it.score)
        //                drop = it
        //
        //            else if (drop.score == it.score) {
        //                if (Random.nextInt(0, 2) == 1)
        //                    drop = it
        //            }
        //        }
        //    }
        //
        //    chest.setItem(slot, ItemStack(drop.material, drop.range.random()))
        //}
    }

    fun destroy () {
        SQL.add("DELETE FROM loot WHERE id='$id'")
        ROA.instance.server.scheduler.cancelTask(particleTaskID)
        instances.remove(this)
    }

    private fun showMarker () {
        for (i in 0..10) {
            location.world!!.spawnParticle(Particle.REDSTONE,
                    markerLocation,
                    1,
                    0.2, 0.4, 0.2,
                    Particle.DustOptions(Color.fromRGB(255,(100..255).random(),0), 0.3f)
            )
        }
    }

    private fun start () {
        markerLocation = Location(location.world, location.x + 0.5, location.y + 0.5, location.z + 0.5)
        particleTaskID = ROA.instance.server.scheduler.scheduleSyncRepeatingTask(ROA.instance, {showMarker()}, 0L, 1L)
    }

    /*==/ REGISTER /==*/
    constructor (c: Container, table: String) {
        lootTable = LootTable.fromName(table)!!
        chest = Bukkit.createInventory(this, 9, "Loot: ${lootTable.name}")
        location = c.location

        start()
        SQL.add("INSERT INTO loot (id, loot_table, world, x, y, z) VALUES ('$id', '${lootTable.name}', '${location.world!!.name}', ${location.x}, ${location.y}, ${location.z})")
        instances.add(this)
    }

    // Load
    constructor (r: ResultSet) {
        id = r.getString("id")
        lootTable = LootTable.fromName(r.getString("loot_table"))!!

        location = Location(
                ROA.instance.server.getWorld(r.getString("world")),
                r.getDouble("x"),
                r.getDouble("y"),
                r.getDouble("z")
        )

        val b = location.block.state
        if (b is Container) {
            chest = Bukkit.createInventory(this, 9, "Loot: ${lootTable.name}")
            start()
            instances.add(this)
            println("...$id")
        }
        // If no container, delete the Loot reference
        else {
            SQL.add("DELETE FROM loot WHERE id='$id'")
            println("...error: No container at location, deleted: $id")
        }
    }

    companion object {
        val instances = mutableListOf<LootzContainer>()
        var timeTillRefill = 0
        private const val refillCooldown = 60 * 60 // 1 hour

        fun fromLocation (l: Location) : LootzContainer? {
            instances.forEach {
                if (it.location == l)
                    return it
            }
            return null
        }

        fun lootTableExists (s: String) : Boolean {
            return LootTable.fromName(s) != null
        }

        fun init () {
            println("[ LOADING LOOT ]")

            LootTable.init()

            with (SQL.connect()) {
                with (prepareStatement("SELECT * FROM loot")) {
                    val r = executeQuery()
                    while (r.next()) { LootzContainer(r) }
                    r.close()
                    close()
                }
                close()
            }

            Timer().schedule(0, 1000) {
                refillTimer()
            }
        }

        fun refillTimer () {
            if (timeTillRefill <= 0) {
                instances.forEach {
                    it.refill()
                }

                // Notify online players.
                Chat.event("Loot has been refilled, good luck!")

                timeTillRefill = refillCooldown
            }
            else if (timeTillRefill.toDouble() / 60.0 == 5.0) {
                // Notify online players.
                Chat.event("5 minutes until loot refills!")
            }

            timeTillRefill --
        }
    }

    /*==/ Loot Table Class /==*/
    class LootTable (val name: String) {
        init {
            instances.add(this)
        }

        companion object {
            val instances = mutableListOf<LootTable>()

            fun fromName (s: String) : LootTable? {
                instances.forEach {
                    if (it.name.equals(s, true))
                        return it
                }
                return null
            }

            fun init () {}
        }
    }

    override fun getInventory(): Inventory {
        return chest
    }
}