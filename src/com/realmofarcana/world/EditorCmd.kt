package com.realmofarcana.world

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.block.Biome
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception

class EditorCmd : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val member = Member.fromID(sender.uniqueId.toString()) ?: return false

        when {
            args[0].equals("set", true) -> {}

            args[0].equals("paint", true) -> {}

            args[0].equals("replace", true) -> {}
        }

        when (args.size) {
            0 -> {}

            1 -> when {
                args[0].equals("del", true) -> {
                }
            }

            2 -> when {
                args[0].equals("setbiome", true) -> {
                    try {
                        for (x in 0..15) {
                            for (z in 0..15) {
                                for (y in 0..sender.location.world!!.maxHeight)
                                    sender.location.chunk.getBlock(x, y, z).biome = Biome.valueOf(args[1].capitalize())
                            }
                        }
                    }
                    catch (e: Exception) {
                        Chat.error(sender, "That isn't a proper biome ID.")
                    }

                    Chat.info(sender, "Changed this chunk's {a}biome{x} to {a}${sender.location.block.biome.name}{x}.")
                }
            }
        }

        return true
    }
}