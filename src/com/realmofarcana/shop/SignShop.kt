package com.realmofarcana.shop

import com.realmofarcana.SQL
import com.realmofarcana.chat.Chat
import com.realmofarcana.member.Member
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.sql.ResultSet

class SignShop {
    var selling = true
    var amount = 0
    var cost = 0
    var location: Location
    lateinit var item: ItemStack
    lateinit var owner: Member
    private var inventory = Bukkit.createInventory(null, 18, "Sign Shop")
    var serverSign = false

    constructor(sign: Sign, amount: Int, material: Material, cost: Int) {
        location = sign.location
        this.amount = amount
        this.cost = cost

        this.item = ItemStack(material, cost)

        instances.add(this)
    }

    // Create and Save to DB
    constructor(owner: String, selling: Boolean, item: ItemStack, location: Location, amount: Int, cost: Int) {
        this.item = item
        this.location = location
        this.amount = amount
        this.cost = cost
        this.selling = selling
    }

    fun openInventory(player: Player) {
        player.openInventory(inventory)
    }

    // The shop is selling an item:
    fun sell(other: Player) {
        // Check if the shop contains the minimum amount of item
        if (!inventory.containsAtLeast(item, amount) && !serverSign) {
            Chat.error(other, "The shop doesn't have enough inventory to sell right now.")
            return
        }

        // Check if the buying player has enough crowns:
        var buyingMember = Member.fromID(other.uniqueId.toString())

        // Check if member exists
        if (buyingMember == null) {
            Chat.error(other, "There was an error with getting a Member object for you.  Please contact an admin.")
            return
        }

        if (buyingMember.crowns >= cost) {
            Chat.error(other, "You can't afford the cost of the item.")
            return
        }

        buyingMember.crowns -= cost

        if (!serverSign) {
            owner.crowns += cost
        }

        // Give the player the item
        other.inventory.addItem(item)
    }

    // The shop is buying:
    fun buy(other: Player) {}

    companion object {
        val instances = mutableListOf<SignShop>()

        // Is the sign a shop:
        fun getShop(sign: Block): SignShop? {
            instances.forEach {
                if (it.location == sign.location)
                    return it
            }
            return null
        }

        fun init() {
            /*println("[ LOADING SIGN SHOPS ]")

            val con = SQL.connect()
            val stm = con.createStatement()
            val r = stm.executeQuery("SELECT * FROM sign_shops")

            while (r.next()) SignShop(r)
            r.close()
            stm.close()
            con.close()*/
        }
    }
}