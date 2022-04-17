package com.realmofarcana.member

import com.realmofarcana.chat.Chat
import com.realmofarcana.ROA
import com.realmofarcana.region.Region
import com.realmofarcana.SQL
import com.realmofarcana.clan.Clan
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import java.sql.ResultSet

class Member {
    var id = "" // The UUID of the member
    var username = "" // The in-game name of the user.
    var bio = "" // RP-use - the background/biography of a character
    var race = "" // RP-use - the race of a character
    var rank = Rank.default
    var clan: Clan? = null // What clan does the player belong in?
    var player: Player? = null // The MC Player object belonging to this member
    var permAttachment: PermissionAttachment? = null // For ranks/permissions
    var crowns = 100 // Crowns are going to be permanent objects in the world, and not digital
    var land = Region.fromId(id) // Current player owned Region the player is in
    var landTitle = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SEGMENTED_20)
    var whereAmI: Region? = null // Current region the player is in
    var canHearth = 0 // If canHearth is equal to zero they can hearth, else they can't (Cooldown)

    var bypass = false // Region bypassing for admins
    var regionEdit : Region? = null // The current region being edited by an admin

    var friends = mutableListOf<String>()

    var clanRequest : Clan? = null // Does the player have a clan request?
    var friendRequest : Member? = null // Does the player have a friend request?

    fun getCS() {
        var output = "Character Sheet\nName: $username"
        output += "Race: $race\nBIO: $bio"
    }

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

    fun addFriend(other: Member) {

    }

    fun removeFriend(other: Member) {}

    fun isFriend() {}

    fun landAvailable () : Boolean { return (land?.chunks?.size ?: 0) < rank.maxChunks }

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

        with (SQL.connect()) {
            with (prepareStatement("SELECT * FROM friends WHERE uuid=?")) {
                setString(1, id)

                val friendResult = executeQuery()
                // Go through each chunk from the query
                while (friendResult.next()) {
                    // Add new chunk to list of chunks
                    friends.add(friendResult.getString("other"))
                }
                friendResult.close() // Close the current query
                close() // Close the current prepared statement
            }
            close() // Close the connection to SQL
        }

        instances.add(this)
        println("...$username")
    }

    companion object {
        val instances = mutableListOf<Member>()

        fun init () {
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
}