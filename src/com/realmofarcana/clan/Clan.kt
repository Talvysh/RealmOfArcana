package com.realmofarcana.clan

import com.realmofarcana.chat.Chat
import com.realmofarcana.Helper
import com.realmofarcana.SQL
import com.realmofarcana.member.Member
import java.sql.ResultSet
import java.util.*
import kotlin.concurrent.schedule

class Clan {
    var id = UUID.randomUUID().toString()
    var name = "Clan"
    var maxMembers = 20

    private val members = mutableMapOf<Member, ClanRank>()

    fun invite (m: Member, targetName: String) {
        val p = m.player!!
        val target = Member.fromName(targetName)

        when {
            target == null -> Chat.info(p, "That player doesn't exist.")

            target.player == null -> Chat.info(p, "That player needs to be online in order to accept an invite from you.")

            target.clan != null -> Chat.info(p, "That player is already in a clan.")

            members.size >= maxMembers -> Chat.info(p, "You cannot invite any more members.")

            else -> {
                target.clanRequest = this

                // Remove the clan request after 60 seconds.
                Timer().schedule(0, 60*1000) {
                    target.clanRequest = null

                    // If the invite wasn't accepted, notify them.
                    if (m.clan != target.clan) {
                        Chat.info(m.player, "Your invite for {a}${target.username}{x} timed out.")
                        Chat.info(target.player, "Your invite to {a}$name{x} timed out.")
                    }
                }
            }
        }
    }

    fun newMember (m: Member) {
        when {
            m.clan != null -> Chat.error(m.player!!, "You are in a clan already.", "/clan leave")

            members.size >= maxMembers -> Chat.error(m.player!!, "{a}$name{x} is now {r}full{x}, and can't accept more invite.")

            else -> {
                SQL.addList(listOf("UPDATE members SET clan='$name', clan_rank='${ClanRank.MEMBER.name}' WHERE id='${m.id}'"))
                members[m] = ClanRank.MEMBER

                members.forEach {
                    if (it.key.player != null)
                        Chat.info(it.key.player!!, "{a}${m.username}{x} has joined your clan!")
                }
            }
        }
    }

    fun kick (n: String) {
    }

    fun disband (m: Member) {
        if (isOwner(m)) {
            SQL.addList(listOf("DELETE FROM clans WHERE id=$id"))

            members.forEach {
                SQL.addList(listOf("UPDATE members SET clan='', clan_rank='', WHERE id=${it.key.id}"))
                it.key.clan = null

                if (it.key.player != null)
                    Chat.info(it.key.player!!, "Your clan has {r}{+}disbanded{x}!")
            }

            instances.remove(this)
        }
        else Chat.error(m.player!!, "You are not the owner of this clan.", "/clan leave")
    }

    fun hearth (m: Member, targetName: String) {
        val target = Member.fromName(targetName)

        when {
            target == null -> Chat.error(m.player, "That player doesn't exist.")
            target.land == null -> Chat.error(m.player, "That player doesn't have any land to hearth to.")
            !members.containsKey(target) -> Chat.error(m.player, "That player isn't in your clan.")
            m.canHearth > 0 -> Chat.error(m.player, "You cannot hearth for another ${m.canHearth} seconds.")
            else -> {
                Chat.info(m.player, "Hearthing you to ${target.username}.")
                m.player!!.teleport(target.land!!.hearth)
                m.canHearth = m.rank.etherealCooldown
            }
        }
    }

    fun printInfo(): String {
        var msg = "${Chat.title("{y}$name")}\n{a}Members{x}: "

        members.forEach {
            msg += when (it.value) {
                ClanRank.OWNER -> "[{a}★{x}] ${it.key.username}, "
                else -> "${it.key.username}, "
            }
        }

        return msg.removeRange(msg.lastIndex-1, msg.lastIndex)
    }

    fun isOwner (member: Member): Boolean { return (members[member] == ClanRank.OWNER) }

    fun isOfficer(member: Member): Boolean { return (members[member]!!.compareTo(ClanRank.OFFICER) >= 0) }

    // Register
    constructor(name: String, owner: Member) {
        this.name = name
        owner.clan = this
        members[owner] = ClanRank.OWNER

        SQL.addList(listOf(
                "INSERT INTO clans (id, name) VALUES ('$id', '$name')",
                "UPDATE members SET clan='$id', clan_rank='${ClanRank.OWNER.name}' WHERE id='${owner.id}'"
            )
        )

        instances.add(this)
    }

    // Load
    constructor(r: ResultSet) {
        id = r.getString("id")
        name = r.getString("name")

        with (SQL.connect()) {
            with (prepareStatement("SELECT id, clan_rank FROM members WHERE clan=?")) {
                setString(1, id)
                // Members query
                val mq = executeQuery()

                while (mq.next()) {
                    val m = Member.fromID(mq.getString("id")) ?: continue
                    val mr = ClanRank.valueOf(mq.getString("clan_rank"))

                    members[m] = mr
                    m.clan = this@Clan
                }
                mq.close()
                close()
            }
            close()
        }

        instances.add(this)
        print("...$name")
    }

    companion object {
        val instances = mutableListOf<Clan>()
        val nameLength = 3..16

        enum class ClanRank { MEMBER, OFFICER, OWNER }

        fun fromName (n: String): Clan? {
            instances.forEach {
                if (it.name.equals(n, true)) return it
            }
            return null
        }

        fun newClan (m: Member, s: String) {
            /* m.player is not null, or else they
             * couldn't have sent a command. */
            val p = m.player!!

            when {
                s.length !in nameLength ->
                    Chat.error(p, "{r}Clan name{x} must be a length from {a}{/}${nameLength.first}{x} to {a}{/}${nameLength.last}{x}.", "/help clan creation")

                !Helper.isName(s) ->
                    Chat.error(p, "Clan name can only contain letters.")

                m.clan != null ->
                    Chat.error(p, "You are already part of a clan.", "/clan leave, /help clan creation")

                fromName(s) != null ->
                    Chat.error(p, "'{r}$s{x}' is already taken by another clan.")

                else -> {
                    m.clan = Clan(s, m)
                    Chat.info(p, "You are now the owner of {a}$s{x}, {y}congrats{x}!")
                }
            }
        }

        fun rename (member: Member, oldName: String, newName: String) {
            if (!member.hasPerm("roa.clan.admin")) {
                Chat.error(member.player, "")
                return
            }
        }

        fun adminDisband (member: Member, clanName: String) {

        }

        fun init() {
            println("[ LOADING CLANS ]")

            with (SQL.connect()) {
                with (prepareStatement("SELECT * FROM clans")) {
                    val r = executeQuery()
                    while (r.next()) Clan(r)
                    close()
                }
                close()
            }
        }
    }
}