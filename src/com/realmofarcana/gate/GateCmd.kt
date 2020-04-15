package com.realmofarcana.gate

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.concurrent.schedule

class GateCmd: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        val member = Member.fromID(sender.uniqueId.toString()) ?: return false

        when (args.size) {
            0 -> {
                var message = Chat.title("{a}Ethereal Gates")

                Gate.instances.forEach {
                    if (it.access == "public")
                        message += "${it.name}, "
                    else if (member.hasPerm("roa.gates.vet") && it.access == "roa.gates.vet")
                        message += "${it.name}, "
                }

                Chat.raw(sender, message.removeRange(message.lastIndex-1, message.lastIndex))
            }

            1 -> {
                val target = Gate.fromName(args[0])

                when {
                    target == null ->
                        Chat.info(sender, "${args[0]} doesn't exist.\n${ChatColor.AQUA}Type '/gate' for a list of gates avilable to you.")

                    member.canHearth > 0 ->
                        Chat.info(sender, "Cannot travel the {g}{/}ethereal plane{x} for another ${member.canHearth} seconds.")

                    target.access == "roa.gates.vet" && !member.hasPerm("roa.gates.vet") ->
                        Chat.info(sender, "Only admins can access ${target.name}.")

                    else -> {
                        sender.teleport(target.location)
                        Chat.info(sender, "Traveled the {a}{/}ethereal plane{x} to ${target.name}.")

                        member.canHearth = member.rank.etherealCooldown

                        Timer().schedule(0, 1000) {
                            member.canHearth--

                            if (member.canHearth <= 0)
                                cancel()
                        }
                    }
                }
            }

            2 -> {
                 if (!member.hasPerm("roa.gates.admin")) {
                     Chat.info(sender, Chat.ADMIN_COMMAND_ONLY)
                     return true
                 }

                 var gate = Gate.fromName(args[1])

                 // If the hearth doesn't exist and the player is not trying to 'set' a 'new' hearth.
                 if (gate == null && !args[0].equals("bind", true)) {
                     Chat.info(sender, "${args[1]} doesn't exist.")
                     return false
                 }

                 when {
                     args[0].equals("bind", true) -> {
                         if (gate == null) gate = Gate(args[1], sender.location, "public")
                         else gate.updateLocation(sender.location)

                         Chat.info(sender, "${gate.name} has been bound to the Earth.")
                     }

                     args[0].equals("destroy", true) && gate != null -> {
                         Member.instances.forEach {
                             if (it.player != null)
                                 Chat.info(sender, "${gate.name} has been destroyed!")
                         }
                         gate.destroy()
                     }
                 }
             }

            3 -> {}
        }
        return true
    }
}