package com.realmofarcana.region

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.concurrent.schedule

class LandCmd : CommandExecutor {
    lateinit var sender: Player
    lateinit var label: String
    lateinit var args: Array<out String>
    lateinit var member: Member

    override fun onCommand(_sender: CommandSender, command: Command, _label: String, _args: Array<out String>): Boolean {
        if (_sender !is Player) return false

        this.sender = _sender
        this.label = _label
        this.args = _args

        member = Member.fromID(sender.uniqueId.toString()) ?: return false

        when {
            label.equals("hearth", true) -> hearth()
            label.equals("land", true) -> {
                when (args.size) {
                    0 -> landInfo()

                    1 -> {
                        when {
                            args[0].equals("claim", true) -> claim()
                            args[0].equals("unclaim", true) -> unclaim()
                            args[0].equals("sethearth", true) -> setHearth()
                            else -> Chat.info(sender, Chat.COMMAND_DOES_NOT_EXIST)
                        }
                    }

                    else -> Chat.info(sender, Chat.COMMAND_DOES_NOT_EXIST)
                }
            }
        }
        return true
    }

    fun landInfo() {
        if (member.land == null) {
            Chat.error(sender, "You don't have any land of your own.", "/land claim")
            return
        }

        val hearth = member.land!!.hearth

        var message = Chat.title("{a}Your Land")
        message += "  • {a}Hearth{x}: ${hearth.world!!.name}: ${hearth.x.toInt()}/${hearth.y.toInt()}/${hearth.z.toInt()}\n"
        message += "  • {a}Available Chunks{x}: ${member.land!!.chunks.size}/{r}${member.rank.maxChunks}\n"

        Chat.raw(sender, message)
    }

    fun claim() {
        val target = Region.fromChunk(sender.location.chunk)

        // Check that target chunk is not owned
        if (target != null) {
            Chat.error(sender, "You can't claim this chunk.", "/? land claim")
            return
        }

        /*==/ New Land /==*/
        if (member.land == null) {
            member.land = Region(Region.Companion.RegionType.PLAYER, sender.location.chunk, member.id, member)
            member.land!!.hearth = sender.location
            Chat.info(sender, "{a}{/}Claimed{x} your {+}first chunk{x}!")
            return
        }

        /* Land available? */
        if (member.rank.maxChunks <= member.land!!.chunks.size) {
            Chat.info(sender, "You {r}{/}cannot claim{x} more chunks.")
            return
        }

        member.land!!.addChunk(sender.location.chunk)
        Chat.info(sender, "You {a}{/}claimed this chunk{x}.")
    }

    fun unclaim() {
        if (member.land == null){
            Chat.info(sender, "You have no land to unclaim.")
            return
        }
        else if (!member.land!!.ownsChunk(sender.location.chunk)) {
            Chat.info(sender, "You can't abandon this chunk.")
            return
        }

        member.land!!.removeChunk(sender.location.chunk)

        if (member.land!!.chunks.size <= 0) {
            member.land = null
            Chat.info(sender, "You have no more chunks, and abandoned your land.")
        }
        else Chat.info(sender, "Abandoned this chunk from your land.")
    }

    fun hearth() {
        if (member.land == null) {
            Chat.error(sender, "You do not have a land of your own.", "/land claim")
            return
        }

        if (member.canHearth > 0) {
            Chat.error(sender, "Cannot travel the ethereal plane for another {r}{/}${member.canHearth}{x} seconds.")
            return
        }

        sender.teleport(member.land!!.hearth)
        Chat.info(sender, "Traveled the {a}{/}ethereal plane{x} to your land's hearth.")

        member.canHearth = member.rank.etherealCooldown
        Timer().schedule(0, 1000) {
            member.canHearth--

            if (member.canHearth <= 0) cancel()
        }
    }

    fun setHearth() {
        if (member.land == null) {
            Chat.error(sender, "You do not have a land of your own.", "/land claim")
            return
        }

        member.land!!.updateHearth(sender.location)
        Chat.info(sender, "Your land's hearth is now set to your location.")
    }
}