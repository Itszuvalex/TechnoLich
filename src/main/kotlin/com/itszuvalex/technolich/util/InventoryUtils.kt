package com.itszuvalex.technolich.util

import com.itszuvalex.technolich.api.adapters.IItemStack
import com.itszuvalex.technolich.api.adapters.ILevel
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers

object InventoryUtils {
    /**
     * Drops [item] into the world at [pos], scattered as vanilla does when a container block breaks. Does nothing for
     * an empty stack.
     */
    @JvmStatic
    fun dropItem(level: ILevel, pos: BlockPos, item: IItemStack) {
        if (item.isEmpty()) return
        Containers.dropItemStack(level.toMinecraft(), pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), item.toMinecraft())
    }
}
