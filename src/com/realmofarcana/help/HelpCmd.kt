package com.realmofarcana.help

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HelpCmd : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, l: String, a: Array<out String>): Boolean {
        if (sender !is Player) return false

        if (a.size == 1) {
            if (a[0].equals("reload", true)) {
                return if (Member.fromID(sender.uniqueId.toString())!!.hasPerm("roa.rank")) {
                    Help.instances.clear()
                    Help.init()
                    Chat.info(sender, "Reloading Help...")
                    true
                }
                else {
                    Chat.info(sender, Chat.ADMIN_COMMAND_ONLY)
                    true
                }
            }
        }

        val label : String = a.joinToString(" ")

        if (a.isEmpty()) {
            Help.instances.forEach {
                if (it.label.equals("main", true)) {
                    Chat.raw(sender, it.print())
                    return true
                }
            }
        }

        Help.instances.forEach {
            if (it.label.equals(label, true)) {
                Chat.raw(sender, it.print())
                return true
            }
        }

        Chat.error(sender, "Couldn't find a help page for that.", "/?")
        return true
    }
}