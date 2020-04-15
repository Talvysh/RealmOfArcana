package com.realmofarcana.member

import com.realmofarcana.chat.Chat
import com.realmofarcana.ROA
import com.realmofarcana.region.Region
import com.realmofarcana.SQL
import com.realmofarcana.clan.Clan
import com.realmofarcana.world.Editor
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import java.io.File
import java.sql.ResultSet

class Member {
    var username = ""
    var rank = Rank.default
    var id = ""
    var clan: Clan? = null
    var player: Player? = null
    var permAttachment: PermissionAttachment? = null
    var crowns = 100
    var land = Region.fromId(id)
    var landTitle = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SEGMENTED_20)
    var whereAmI: Region? = null
    var canHearth = 0

    var bypass = false
    var regionEdit : Region? = null
    var editor = Editor()

    var clanRequest : Clan? = null
    var friendRequest : Member? = null

    fun setRank (admin: Member, rankName: String) {
        if (!admin.hasPerm("roa.admin.rank")) {
            Chat.error(admin.player, "You do not have the permission to set other's ranks.")
            return
        }

        rank = Rank.getByName(rankName)
        Chat.info(admin.player, "{a}$username{x} is now {a}${rank.name}{x}.")
        Chat.info(player, "You are now a {a}${rank.name}{x}.")

        SQL.add("UPDATE members SET rank='${rank.name}' WHERE id='$id'")
    }

    fun hasPerm (perm: String) : Boolean { return rank.perms.contains(perm) }

    fun setPerms () {
        permAttachment = player!!.addAttachment(ROA.instance)

        rank.perms.forEach {
            permAttachment!!.setPermission(it, true)
        }
    }

    fun removePerms () {
        player!!.removeAttachment(permAttachment!!)
        permAttachment = null
    }

    fun landAvailable () : Boolean { return land?.chunks?.size ?: 0 < rank.maxChunks }

    /*==/ REGISTER /==*/
    constructor (p: Player) {
        username = p.name
        rank     = Rank.default
        id       = p.uniqueId.toString()
        player   = p
        crowns   = 100

        SQL.add("INSERT INTO members (id, rank, username, crowns) VALUES ('$id', '${rank.name}', '$username', $crowns)")
        instances.add(this)
    }

    /*==/ LOAD /==*/
    constructor (r: ResultSet) {
        id       = r.getString("id")
        username = r.getString("username")
        rank     = Rank.getByName(r.getString("rank"))
        crowns   = r.getInt("crowns")

        instances.add(this)
        println("...$username")
    }

    companion object {
        val instances = mutableListOf<Member>()

        fun init () {
            Rank.init()

            with (SQL.connect()) {
                with (prepareStatement("SELECT * FROM members")){
                    println("[ LOADING MEMBERS ]")
                    val r = executeQuery()
                    while(r.next()) Member(r)
                    close()
                }
                close()
            }
        }

        fun fromID (uuid: String): Member? {
            instances.forEach {
                if (it.id.equals(uuid, false))
                    return it
            }
            return null
        }

        fun fromName (name: String): Member? {
            instances.forEach {
                if (it.username.equals(name, true))
                    return it
            }
            return null
        }
    }

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

                config.getKeys(false).forEach {
                    Rank(
                            it
                    )
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
}