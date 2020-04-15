package com.realmofarcana.lootz

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.ChatColor
import org.bukkit.block.Chest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LootzCmd : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val member = Member.fromID(sender.uniqueId.toString()) ?: return false

        if (!member.hasPerm("roa.loot.admin")) {
            Chat.info(sender, Chat.ADMIN_COMMAND_ONLY)
            return true
        }

        when (args.size) {
            0 -> {
                // TODO: Loot information
            }

            1 -> when {
                args[0].equals("refill", true) -> {
                    LootzContainer.timeTillRefill = 0
                    LootzContainer.refillTimer()
                }

                args[0].equals("list", true) -> {
                    var tables = Chat.title("List of Loot Tables")

                    for (i in 0 until LootzContainer.LootTable.instances.size) {
                        tables += if (i % 2 == 0) "{x}"
                        else "{gr}"
                        tables += LootzContainer.LootTable.instances[i].name + ", "
                    }

                    tables = tables.removeRange(tables.lastIndex - 1..tables.lastIndex)

                    Chat.info(sender, tables)
                }

                args[0].equals("destroy", true) -> {
                    val block = sender.getTargetBlockExact(16)
                    val loot = LootzContainer.fromLocation(block!!.location)

                    if (loot == null) {
                        Chat.info(sender, "There is no loot set to this block.")
                        return true
                    }

                    loot.destroy()
                    Chat.info(sender, "Destroyed the link to the container.")
                }

                args[0].equals("reload", true) -> {
                    LootzContainer.instances.clear()
                    LootzContainer.init()
                    Chat.info(sender, "Reloading loot...")
                }
            }

            2 -> when {
                args[0].equals("set", true) -> {
                    val tableName = args[1]
                    val block = sender.getTargetBlockExact(16)!!.state

                    // If the target block is a chest.
                    if (block !is Chest) {
                        Chat.info(sender, "Target block must be a chest.")
                        return true
                    }

                    val lootz = LootzContainer.fromLocation(block.location)
                    // Update LootzTable if Lootz exists.
                    if (lootz != null) {
                        //lootz.updateTable(args[1])
                        return true
                    }

                    if (!LootzContainer.lootTableExists(args[1])) {
                        var message = "That loot table doesn't exist.\n${ChatColor.DARK_AQUA}Available Tables: ${ChatColor.AQUA}"
                        // TODO: Show available tables
                        Chat.info(sender, message)
                        return true
                    }

                    LootzContainer(block, args[1])
                    Chat.info(sender, "Set loot for the container.")
                }
            }
        }

        return true
    }
}