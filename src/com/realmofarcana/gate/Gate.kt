package com.realmofarcana.gate

import com.realmofarcana.ROA
import com.realmofarcana.SQL
import org.bukkit.Location
import java.sql.ResultSet

class Gate {
    var name = ""
    var location: Location
    var access = "roa.gate"

    fun updateName(n: String) {
        SQL.add("UPDATE gates SET name='$n' WHERE name='$name'")
        name = n
    }

    fun updateLocation(l: Location) {
        location = l
        SQL.add("UPDATE gates SET world='${location.world!!.name}', x=${location.x}, y=${location.y}, z=${location.z} WHERE name='$name'")
    }

    fun destroy() {
        instances.remove(this)
        SQL.add("DELETE FROM gates WHERE name='$name'")
    }

    /*==/ REGISTER /==*/
    constructor(name: String, location: Location, access: String) {
        this.name     = name
        this.location = location
        this.access   = access

        SQL.addList(listOf(
            """
            INSERT INTO gates (name, access, world, x, y, z)
            VALUES ('$name', '$access', '${location.world!!.name}', ${location.x}, ${location.y}, ${location.z})
            """
        ))

        instances.add(this)
    }

    /*==/ LOAD /==*/
    constructor(r: ResultSet) {
        name     = r.getString("name")
        access   = r.getString("access")
        location = Location(
            ROA.instance.server.getWorld(r.getString("world") ?: "world"),
            r.getDouble("x"),
            r.getDouble("y"),
            r.getDouble("z")
        )

        instances.add(this)
        println("...$name")
    }

    companion object {
        val instances = mutableListOf<Gate>()

        fun fromName (n: String) : Gate? {
            instances.forEach {
                if (it.name.equals(n, true))
                    return it
            }
            return null
        }

        fun init() {
            println("[ LOADING ETHEREAL GATES ]")
            with (SQL.connect()) {
                with (prepareStatement("SELECT * FROM gates")) {
                    val r = executeQuery()
                    while (r.next()) {
                        Gate(r)
                    }
                    close()
                }
                close()
            }
        }
    }
}