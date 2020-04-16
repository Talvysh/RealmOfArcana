package com.realmofarcana.member

import com.realmofarcana.chat.Chat
import com.realmofarcana.world.World
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class MemberCommands: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        // Get a Member object
        val member = Member.fromID(sender.uniqueId.toString()) ?: return true


        when {
            label.equals("member", true) -> {}

            label.equals("rank", true) -> {
                val targetName = args[0]
                val rankName = args[1]

                if (!member.hasPerm("roa.admin.rank") && !sender.isOp) {
                    Chat.error(sender, Chat.ADMIN_COMMAND_ONLY)
                    return true
                }

                val target = Member.fromName(targetName)
                when (target) {
                    null -> Chat.error(sender, "Couldn't find that player.")
                    else -> target.setRank(member, rankName)
                }
            }

            label.equals("culling", true) -> {
                if (member.hasPerm("roa.culling")){
                    when (args.size) {
                        1 -> {
                            when {
                                args[0].equals("start", true) -> World.startCulling()
                                args[0].equals("end", true) -> World.endCulling()
                                else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                            }
                        }
                        else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
                    }
                }
                else Chat.error(sender, Chat.ADMIN_COMMAND_ONLY)
            }

            label.equals("uenchant", true) -> {
                try {
                    val enchant = Enchantment.getByKey(NamespacedKey.minecraft(args[0]))!!
                    val item = sender.inventory.itemInMainHand
                    item.addUnsafeEnchantment(enchant, args[1].toInt())
                    Chat.info(member.player, "Added an unsafe enchantment to this item: ${enchant.key.key}: ${args[1]}")
                } catch (e: Exception) {
                    Chat.error(member.player, "That enchantment doesn't exist.")
                }
            }

            else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST)
        }

        return true
    }

    private fun setRank () {}

    private fun culling () {}

    private fun bypass () {}

    private fun fly () {}
}