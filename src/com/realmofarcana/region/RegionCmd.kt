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
        if (sender !is Player) return false

        /* Member and perm check. */
        member = Member.fromID(sender.uniqueId.toString()) ?: return true
        if (!member.hasPerm("roa.region")) {
            Chat.error(sender, Chat.ADMIN_COMMAND_ONLY)
            return true
        }

        region = Region.fromChunk(sender.location.chunk)
        if (label.equals("region", true)) {
            when (args.size){
                /* Region LIST */
                0 -> {
                    var message = Chat.title("{Y}Regions")

                    /* List all non-player regions. */
                    Region.instances.forEach {
                        if (it.type != RegionType.PLAYER) {
                            message += "${it.id}, "
                        }
                    }

                    Chat.raw(sender, message.removeRange(message.lastIndex-1, message.lastIndex))
                }

                /*==/ CLAIM /==*/
                1 -> when {
                    args[0].equals("claim", true) -> {
                        if (member.regionEdit == null) {
                            Chat.error(sender, "You must first select a region to edit!", "/region edit <name>")
                            return true
                        }

                        member.regionEdit!!.addChunk(sender.location.chunk)
                        Chat.info(sender, "Chunk {a}{/}claimed{x} for ${member.regionEdit!!.id}")
                    }

                    args[0].equals("unclaim", true) -> {
                        if (member.regionEdit == null) {
                            Chat.error(sender, "You must first select a region to edit!", "/region edit <name>")
                            return true
                        }

                        member.regionEdit!!.removeChunk(sender.location.chunk)
                        Chat.info(sender, "Chunk {r}{/}unclaimed{x} for ${member.regionEdit!!.id}")
                    }

                    args[0].equals("autoclaim", true) -> {

                    }
                    else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                }

                2 -> when {
                    // Select a Region for editing.
                    args[0].equals("edit", true) -> {
                        val targetName = args[1]
                        val target = Region.fromId(targetName)

                        if (target == null) {
                            Chat.error(sender, "{y}$targetName{x}, doesn't exist.","/region create <name>")
                            return true
                        }

                        member.regionEdit = target
                        Chat.info(sender, "Set region editing to: ${target.getTitle()}.")
                    }

                    /* DESTROY */
                    args[0].equals("destroy", true) -> {
                        val targetName = args[1]
                        val target = Region.fromId(targetName)

                        if (target == null) {
                            Chat.error(sender, "{y}$targetName{x}, doesn't exist.","/region create <name>")
                            return true
                        }

                        target.destroy()
                        Chat.info(sender, "{R}{+}Destroyed{x} {r}${target.id}{x}!")
                    }

                    else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                }

                3 -> when {
                    // Create a new region
                    args[0].equals("create", true) -> {
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
                }
            }
        }

        else if (label.equals("bypass", true)) {
            if (!member.hasPerm("roa.region")) {
                Chat.error(sender, Chat.ADMIN_COMMAND_ONLY)
                return true
            }

            member.bypass = !member.bypass
            Chat.info(sender, "Region bypass set to: {a}{/}${member.bypass}{x}.")
        }

        return true
    }

    fun checkRegionEdit () : Boolean {

        return false
    }
}