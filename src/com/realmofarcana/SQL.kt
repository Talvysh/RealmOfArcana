package com.realmofarcana

import java.sql.Connection
import java.sql.DriverManager

object SQL {
    private val host     = ROA.instance.config.getString("host")!!
    private val db       = ROA.instance.config.getString("db")!!
    private val user     = ROA.instance.config.getString("user")!!
    private val password = ROA.instance.config.getString("password")!!
    private val updates  = mutableListOf<String>()

    // Set this to true if working on a test server.  Will save all data to YML files.
    private val localOnly = false

    fun add (s: String) { updates.add(s) }

    fun addList (strings: List<String>) { updates.addAll(strings) }

    fun update () {
        // Run updates on a new thread so that it doesn't slow the rest of the server down:
        Thread().run {
            with(connect()) {
                updates.forEach {
                    val stm = this.createStatement()
                    stm.execute(it)
                    stm.close()
                }
                updates.clear()
                close()

                println("Data saved!")
            }
        }
    }

    fun connect () : Connection {
        Class.forName("com.mysql.jdbc.Driver")
        return DriverManager.getConnection("jdbc:mysql://$host/$db?user=$user") //&password=$password
    }
}