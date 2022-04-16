package com.realmofarcana

import com.realmofarcana.clan.Clan
import com.realmofarcana.clan.ClanCommands
import com.realmofarcana.gate.Gate
import com.realmofarcana.gate.GateCmd
import com.realmofarcana.help.Help
import com.realmofarcana.help.HelpCmd
import com.realmofarcana.member.Member
import com.realmofarcana.member.MemberCommands
import com.realmofarcana.member.MemberEvents
import com.realmofarcana.member.Rank
import com.realmofarcana.region.LandCmd
import com.realmofarcana.region.Region
import com.realmofarcana.region.RegionCmd
import com.realmofarcana.region.RegionEvents
import com.realmofarcana.world.World
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.concurrent.schedule

class ROA: JavaPlugin() {
    companion object {
        lateinit var instance: ROA
        var log = Bukkit.getLogger()
    }

    override fun onEnable() {
        saveDefaultConfig()
        instance = this

        Help.init()
        Rank.init()
        Member.init()
        Region.init()
        Gate.init()
        //Clan.init()

        // Register events.
        server.pluginManager.registerEvents(MemberEvents(),this)
        server.pluginManager.registerEvents(RegionEvents(), this)

        // Register commands.
        this.getCommand("rank")!!.setExecutor(MemberCommands())
        this.getCommand("culling")!!.setExecutor(MemberCommands())
        this.getCommand("uenchant")!!.setExecutor(MemberCommands())

        this.getCommand("gate")!!.setExecutor(GateCmd())

        this.getCommand("region")!!.setExecutor(RegionCmd())
        this.getCommand("land")!!.setExecutor(LandCmd())
        this.getCommand("bypass")!!.setExecutor(RegionCmd())

        //this.getCommand("clan")!!.setExecutor(ClanCommands())
        this.getCommand("?")!!.setExecutor(HelpCmd())

        World.start()

        // Update DB every 10 minutes.
        Timer().schedule(600 * 1000, 600 * 1000) { SQL.update() }
    }

    override fun onDisable() {
        SQL.update()
    }
}