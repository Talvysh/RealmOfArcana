package com.realmofarcana.region

import com.realmofarcana.chat.Chat
import com.realmofarcana.ROA
import com.realmofarcana.member.Member
import com.realmofarcana.SQL
import com.realmofarcana.world.World
import com.sun.org.apache.xpath.internal.operations.Bool
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import java.sql.ResultSet
import java.util.*

class Region {
    /*
    * Land ownership system.  Uses chunks to easily
    * claim and abandon pieces of land.
    *
    * Also includes "special" land types such as
    * dungeons, or areas like a spawn, or a POI
    * */

    var id = UUID.randomUUID().toString()
    var type : RegionType
    var color = ChatColor.YELLOW.toString()
    var owner : Member? = null // QOL
    var hearth : Location
    val chunks = mutableListOf<SmartChunk>()

    val flags = mapOf(
        "PVP" to true, "Buildable" to true, "" to false
    )

    fun addChunk (c: Chunk) {
        chunks.add(SmartChunk(c.world.name, c.x, c.z))
        SQL.add("INSERT INTO chunks (region, world, x, z) VALUES ('$id', '${c.world.name}', ${c.x}, ${c.z})")
    }

    fun removeChunk (c: Chunk) {
        chunks.forEach {
            if (it.world.equals(c.world.name, true) && it.x == c.x && it.z == c.z) {
                chunks.remove(it)
                if (chunks.size <= 0) destroy()
                SQL.add("DELETE FROM chunks WHERE world='${it.world}' AND x=${it.x} AND z=${it.z}")
            }
        }

    }

    fun destroy () {
        val removalList = mutableListOf<String>()

        chunks.forEach {
            removalList.add("DELETE FROM chunks WHERE world='${it.world}' AND x=${it.x} AND z=${it.z}")
        }

        removalList.add("DELETE FROM regions WHERE id='$id'")
        SQL.addList(removalList)

        instances.remove(this)
    }

    fun ownsChunk (c: Chunk) : Boolean {
        chunks.forEach {
            if (it.world == c.world.name && it.x == c.x && it.z == c.z)
                return true
        }
        return false
    }

    fun canBuild (member: Member): Boolean {
        // If the player is an admin: True.
        if (member.hasPerm("roa.region") && member.bypass) return true

        // Check if the player owns the land, or if The Culling is in-progress.
        else if (type == RegionType.PLAYER) {
            // If the player owns the land: True
            when {
                World.theCulling -> return true
                member == owner -> return true
                member.clan != null -> {
                    if (member.clan == owner?.clan)
                        return true
                }
            }
        }

        // They can't build.
        return false
    }

    fun isOwner(member: Member): Boolean {
        return owner == member
    }

    fun getTitle(): String {
        var msg = ""

        when (type) {
            RegionType.PLAYER -> {
                if (owner != null) {
                    msg = "${ChatColor.AQUA}${owner!!.username}"

                    if (owner!!.clan != null)
                        msg = "<${ChatColor.YELLOW}${owner!!.clan!!.name}${ChatColor.WHITE}> $msg"
                } else msg = "${ChatColor.RED}ERROR"
            }
            RegionType.SANCTUM -> msg = "${ChatColor.YELLOW}$id"
            RegionType.DUNGEON -> msg = "${ChatColor.RED}$id"
        }

        return msg
    }

    fun updateHearth(l: Location) {
        hearth = l

        SQL.add (
            """
            UPDATE regions SET 
            hearth_world='${hearth.world!!.name}', hearth_x=${hearth.x}, hearth_y=${hearth.y}, hearth_z=${hearth.z} 
            WHERE id='$id'
            """
        )
    }

    /*==/ REGISTER /==*/
    constructor (type: RegionType, chunk: Chunk, name: String, owner: Member? = null) {
        this.type   = type
        this.id     = name
        this.owner  = owner
        hearth      = chunk.world.getHighestBlockAt(chunk.x * 16 + 7, chunk.z * 16 + 7).location

        addChunk(chunk)

        SQL.add(
            """
            INSERT INTO regions (id, type, hearth_world, hearth_x, hearth_y, hearth_z) 
            VALUES ('$id', '${type.name}', '${hearth.world!!.name}', ${hearth.x}, ${hearth.y}, ${hearth.z})
            """
        )

        instances.add(this)
    }

    /*==/ LOAD /==*/
    constructor (r: ResultSet) {
        id    = r.getString("id")
        type  = RegionType.valueOf(r.getString("type"))
        owner = Member.fromID(r.getString("id"))

        if (owner != null) owner!!.land = this

        hearth = Location(
                ROA.instance.server.getWorld(r.getString("hearth_world")),
                r.getDouble("hearth_x"),
                r.getDouble("hearth_y"),
                r.getDouble("hearth_z")
        )

        with (SQL.connect()) {
            with (prepareStatement("SELECT * FROM chunks WHERE region=?")) {
                setString(1, id)
                val chunkResult = executeQuery()
                while (chunkResult.next()) {
                    chunks.add(SmartChunk(chunkResult.getString("world"), chunkResult.getInt("x"), chunkResult.getInt("z")))
                }
                chunkResult.close()
                close()
            }
            close()
        }

        instances.add(this)
        println("...$id")
    }

    companion object {
        var instances = mutableListOf<Region>()
        val blacklist = mutableListOf<Material>()

        enum class RegionType { PLAYER, SANCTUM, DUNGEON }

        fun newRegion (m: Member, regionName: String, _type: String) {
            /* Check perm. */
            if (!m.hasPerm("roa.region")) {
                Chat.error(m.player, "")
                return
            }

            try { // Type check
                m.regionEdit = Region(RegionType.valueOf(_type), m.player!!.location.chunk, m.id)
                Chat.info(m.player, "Created a new region, ${m.regionEdit!!.getTitle()}, and set it to your current edit.")
            }
            catch (e: Exception) {
                Chat.error(m.player, "{r}$_type{x} is not a type of region you can create.", "/region types")
                e.printStackTrace()
            }
        }

        fun save () {
        }

        fun fromChunk(chunk: Chunk) : Region? {
            instances.forEach {
                if (it.ownsChunk(chunk))
                    return it
            }
            return null
        }

        fun fromId (s: String) : Region? {
            instances.forEach {
                if (it.id.equals(s, true))
                    return it
            }
            return null
        }

        fun init () {
            println("[ LOADING REGIONS ]")

            val con = SQL.connect()
            val stm = con.createStatement()
            val r = stm.executeQuery("SELECT * FROM regions")

            while (r.next()) Region(r)
            r.close()
            stm.close()
            con.close()

            Material.values().forEach {
                if (it.name.contains("DOOR")) blacklist.add(it)
            }
        }
    }

    class SmartChunk (val world: String, val x: Int, val z: Int) {
        init {
            instances.add(this)
        }

        companion object {
            val instances = mutableListOf<SmartChunk>()
        }
    }
}