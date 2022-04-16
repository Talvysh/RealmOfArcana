package com.realmofarcana.chat

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.util.ChatPaginator

object Chat {
    const val ADMIN_COMMAND_ONLY = "This command can only be used by admins."
    const val COMMAND_DOES_NOT_EXIST = "That command does not exist."

    private val formatTranslations = mapOf(
            "{x}" to ChatColor.RESET.toString(),
            "{_}" to ChatColor.UNDERLINE.toString(),
            "{/}" to ChatColor.ITALIC.toString(),
            "{+}" to ChatColor.BOLD.toString(),
            "{*}" to ChatColor.MAGIC.toString(),

            "{r}" to ChatColor.RED.toString(),
            "{R}" to ChatColor.DARK_RED.toString(),
            "{y}" to ChatColor.YELLOW.toString(),
            "{Y}" to ChatColor.GOLD.toString(),
            "{g}" to ChatColor.GREEN.toString(),
            "{G}" to ChatColor.DARK_GREEN.toString(),
            "{a}" to ChatColor.AQUA.toString(),
            "{A}" to ChatColor.DARK_AQUA.toString(),
            "{b}" to ChatColor.BLUE.toString(),
            "{B}" to ChatColor.DARK_BLUE.toString(),
            "{p}" to ChatColor.LIGHT_PURPLE.toString(),
            "{P}" to ChatColor.DARK_PURPLE.toString(),
            "{gr}" to ChatColor.GRAY.toString(),
            "{GR}" to ChatColor.DARK_GRAY.toString(),
            "{-}" to ChatColor.BLACK.toString()
    )

    // Send general/system messages to the player.
    fun info (p: Player?, message: String) {
        if (p == null) return

        p.sendMessage(formatString("[{a}i{x}] {a}$message\n"))
    }

    fun error (p: Player?, message: String, usage: String? = null) {
        if (p == null) return

        if (usage != null)
            p.sendMessage(formatString("[{r}!{x}]{r} $message\n{gr}{/}$usage\n"))
        else p.sendMessage(formatString("[{r}!{x}]{r} $message\n"))
    }

    fun raw (p: Player?, message: String) {
        if (p == null) return

        p.sendMessage(formatString("$message\n \n"))
    }

    // Send a message to every player on the server.
    fun event (s: String) {
        val msg = formatString("[{r}Event{x}] $s")
        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage("\n$msg\n") // §c: Red, §f: White
        }
    }

    fun formatString (s: String) : String {
        var formatted = s
        formatTranslations.forEach {
            formatted = formatted.replace(it.key, it.value)
        }
        return formatted
    }

    fun title(s: String) : String {
        var message = "\n{GR}"
        for (i in 0..ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH-5)
            message += "-"
        message += "{x}\n$s{x}"

        message += "\n{GR}"
        for (i in 0..ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH-5)
            message += "-"
        message += "{x}\n"

        return formatString(message)
    }

    private fun center (s: String) : String {
        val padding = ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH/2 - s.length/2
        var str = ""

        for (i in 0..padding)
            str += " "

        return str + s
    }
}