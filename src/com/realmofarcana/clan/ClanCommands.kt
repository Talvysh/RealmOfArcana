package com.realmofarcana.clan

import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClanCommands: CommandExecutor {
    override fun onCommand(sender: CommandSender, c: Command, command: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        val member  = Member.fromID(sender.uniqueId.toString()) ?: return false

        when (args.size) {
            0 -> {
                when (member.clan) {
                    null -> Chat.error(sender, "You have no clan.", "/help clan")
                    else -> Chat.raw(sender, member.clan!!.printInfo())
                }
            }

            1 -> when {
                /*==/ DISBAND /==*/
                args[0].equals("disband", true) -> {
                    if (!checkForClan(member)) return true
                    member.clan!!.disband(member)
                }

                /*==/ ACCEPT INVITE /==*/
                args[0].equals("accept", true) -> {
                    when (member.clanRequest) {
                        null -> Chat.error(sender, "You have no clan requests.", "/help accepting clan invites")
                        else -> member.clanRequest!!.newMember(member)
                    }
                }

                else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST, "/help clan")
            }

            2 -> when {
                /*==/ CREATE /==*/
                args[0].equals("create", true) -> Clan.newClan(member, args[1])

                /*==/ INVITE /==*/
                args[0].equals("invite", true) -> {
                    when (member.clan) {
                        null -> Chat.error(sender, "You have no clan.", "/help clan")
                        else -> member.clan!!.invite(member, args[1])
                    }
                }

                /*==/ KICK /==*/
                args[0].equals("kick", true) -> {
                    when (member.clan) {
                        null -> Chat.error(sender, "You have no clan.")
                        else -> member.clan!!.invite(member, args[1])
                    }
                }

                /*==/ Hearth /==*/
                args[0].equals("hearth", true) -> {
                    when (member.clan) {
                        null -> Chat.error(sender, "You have no clan.")
                        else -> member.clan!!.hearth(member, args[1])
                    }
                }

                /*==/ ADMIN DISBAND /==*/
                args[0].equals("disband", true) -> Clan.adminDisband(member, args[1])

                else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST, "/help clan")
            }

            3 -> when {
                /*==/ RENAME /==*/
                args[0].equals("rename", true) -> Clan.rename(member, args[1], args[2])

                else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST, "/help clan")
            }

            else -> Chat.error(sender, Chat.COMMAND_DOES_NOT_EXIST, "/help clan")
        }
        return true
    }

    private fun checkForClan (m: Member) : Boolean {
        if (m.clan == null) {
            Chat.error(m.player, "You have no clan.", "/help clan")
            return false
        }
        return true
    }
}