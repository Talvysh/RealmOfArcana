package com.realmofarcana.help

import com.realmofarcana.chat.Chat
import com.realmofarcana.SQL
import org.bukkit.util.ChatPaginator
import java.sql.ResultSet

class Help (r: ResultSet) {
    var label = ""
    var description = ""
    var example = ""
    var related = ""

    fun print () : String {
        var message = Chat.title("{r}$label")

        // Break up, and format Description properly.
        ChatPaginator.wordWrap(description, ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH).forEach {
            message += "  $it\n"
        }

        // If the Example section is set.
        if (example.isNotBlank())
            message += "\n{GR}{/}  $example{x}"

        // If the Related section is set.
        if (related.isNotBlank()) {
            var more = " \n \n{a}{_}More:{x} "

            with(related.split(",").toTypedArray()) {
                for (i in 0 until size) {
                    more += if (i % 2 == 0) "{x}"
                    else "{gr}"

                    more += "${this[i]}, "
                }
            }

            // Remove the last ', ' from 'more'.
            more = more.removeRange(more.lastIndex - 1, more.lastIndex)

            // Break up, and format more properly.
            ChatPaginator.wordWrap(more,
                    ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH-2).forEach {
                    message += "  $it\n"
            }
        }

        message = Chat.formatString(message)

        return "$message\n"
    }

    init {
        label = r.getString("label")
        description = r.getString("description")
        example = r.getString("example")
        related = r.getString("related")

        instances.add(this)
    }

    companion object {
        val instances = mutableListOf<Help>()

        fun init () {
            with (SQL.connect()) {
                val r = prepareStatement("SELECT * FROM help").executeQuery()
                while (r.next()) Help(r)

                r.close()
                close()
            }
        }
    }
}