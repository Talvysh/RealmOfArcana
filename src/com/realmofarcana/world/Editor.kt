package com.realmofarcana.world

import com.realmofarcana.ROA
import net.minecraft.server.v1_15_R1.Material
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Structure
import org.bukkit.util.BoundingBox

class Editor {
    var boundingBox: BoundingBox? = null
    var firstCorner: Location? = null
    var secondCorner: Location? = null

    fun updateBox () {
        boundingBox = BoundingBox(firstCorner?.x ?: 0.0, firstCorner?.y ?: 0.0, firstCorner?.z ?: 0.0, secondCorner?.x ?: 0.0, secondCorner?.y ?: 0.0, secondCorner?.z ?: 0.0)
    }

    fun getBlocks () : List<Block> {
        val blocks = mutableListOf<Block>()

        if (boundingBox == null) return blocks

        for (x in boundingBox!!.minX.toInt()..boundingBox!!.maxX.toInt()) {
            for (y in boundingBox!!.minY.toInt()..boundingBox!!.maxY.toInt()) {
                for (z in boundingBox!!.minZ.toInt()..boundingBox!!.maxZ.toInt()) {
                    blocks.add(ROA.instance.server.getWorld("world")!!.getBlockAt(x, y, z))
                }
            }
        }

        return blocks
    }
}