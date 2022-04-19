package com.realmofarcana.region

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import com.realmofarcana.region.Region.Companion.RegionType

class RegionCmd : CommandExecutor {
    lateinit var sender: Player
    lateinit var label: String
    lateinit var args: Array<out String>
    lateinit var member: Member

    var region: Region? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false // Not from the command-line.

        // Get Member or return
        member = Member.fromPlayer(sender) ?: return true
        // Check if member has the 'roa.region' permission:
        if (!member.hasPerm("roa.region")) {
            Chat.error(sender, Chat.ADMIN_COMMAND_ONLY)
            return true
        }

        region = Region.fromChunk(sender.location.chunk)
        if (label.equals("region", true)) {
            when (args.size){
                0 -> regionInfo()

                1 -> when {
                    args[0].equals("claim", true) -> claim()
                    args[0].equals("unclaim", true) -> unclaim()
                    args[0].equals("autoclaim", true) -> autoclaim()
                    args[0].equals("bypass", true) -> bypass()
                    args[0].equals("save", true) -> save()
                    args[0].equals("list", true) -> list()
                    args[0].equals("sethearth", true) -> setHearth()
                    args[0].equals("hearth", true) -> hearth()
                    else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                }

                2 -> when {
                    args[0].equals("edit", true) -> edit()
                    args[0].equals("destroy", true) -> destroy()
                    else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                }

                3 -> when {
                    args[0].equals("create", true) -> create()
                    else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                }
            }
        }
        return true
    }

    private fun hearth() {
        if (member.regionEdit == null) {
            Chat.error(sender, "You must first select a region to edit!", "/region edit <name>")
            return
        }

        sender.teleport(member.regionEdit!!.hearth)
        Chat.info(sender, "Traveled the {a}{/}ethereal plane{x} to ${member.regionEdit!!.id}'s hearth.")
    }

    private fun setHearth() {
        if (member.regionEdit == null) {
            Chat.error(sender, "You must first select a region to edit!", "/region edit <name>")
            return
        }
    }

    private fun create() {
        val type = args[1].toUpperCase()
        val id = args[2]

        val nameConflict = Region.fromId(id)
        val chunkConflict = Region.fromChunk(sender.location.chunk)

        when {
            nameConflict != null -> Chat.error(sender, "That region already exists.")

            chunkConflict != null -> Chat.error(sender, "You must stand in an unclaimed chunk before creating a new region.")

            else -> {
                try { // Type check
                    member.regionEdit = Region(RegionType.valueOf(type), sender.location.chunk, id)
                    Chat.info(sender, "Created a new region, ${member.regionEdit!!.getTitle()}, and set it to your current edit.")
                }
                catch (e: Exception) {
                    Chat.error(sender, "{r}$type{x} is not a type of region you can create.", "/region types")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun destroy() {
        val targetName = args[1]
        val target = Region.fromId(targetName)

        if (target == null) {
            Chat.error(sender, "{y}$targetName{x}, doesn't exist.","/region create <name>")
            return
        }

        target.destroy()
        Chat.info(sender, "{R}{+}Destroyed{x} {r}${target.id}{x}!")
    }

    private fun edit() {
        val targetName = args[1]
        val target = Region.fromId(targetName)

        if (target == null) {
            Chat.error(sender, "{y}$targetName{x}, doesn't exist.","/region create <name>")
            return
        }

        member.regionEdit = target
        Chat.info(sender, "Set region editing to: ${target.getTitle()}.")
    }

    private fun regionInfo() {
        TODO("Not yet implemented")
    }

    private fun claim() {
        if (member.regionEdit == null) {
            Chat.error(sender, "You must first select a region to edit!", "/region edit <name>")
            return
        }

        member.regionEdit!!.addChunk(sender.location.chunk)
        Chat.info(sender, "Chunk {a}{/}claimed{x} for ${member.regionEdit!!.id}")
    }

    private fun unclaim() {
        if (member.regionEdit == null) {
            Chat.error(sender, "You must first select a region to edit!", "/region edit <name>")
            return
        }

        member.regionEdit!!.removeChunk(sender.location.chunk)
        Chat.info(sender, "Chunk {r}{/}unclaimed{x} for ${member.regionEdit!!.id}")
    }

    private fun autoclaim() {
        //TODO: implement autoclaiming while moving from chunk-to-chunk
    }

    private fun list() {
        var message = Chat.title("{Y}Regions")

        /* List all non-player regions. */
        Region.instances.forEach {
            if (it.type != RegionType.PLAYER) {
                message += "${it.id}, "
            }
        }

        Chat.raw(sender, message.removeRange(message.lastIndex-1, message.lastIndex))
    }

    private fun bypass() {
        member.bypass = !member.bypass
        Chat.info(sender, "Region bypass set to: {a}{/}${member.bypass}{x}.")
    }

    private fun save() {
        if (member.regionEdit == null) {
            Chat.error(sender, "You are not currently editing a region.")
            return
        }

        member.regionEdit = null
        member.bypass = false
        Chat.info(sender, "You have exited region edit mode.")
    }
}