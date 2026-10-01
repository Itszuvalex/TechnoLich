package com.itszuvalex.technolich.api.adapters

import com.itszuvalex.technolich.api.wrappers.WrapperBlockEntity
import com.itszuvalex.technolich.api.wrappers.WrapperLevel
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

interface IBlockEntity : IModuleProvider {
    fun getBlockPos(): BlockPos

    fun toMinecraft(): BlockEntity

    companion object {
        /**
         * @return The block entity itself if it already implements IBlockEntity (e.g. a BlockEntityCore), so modules
         * without a block capability stay reachable; otherwise a capability-backed wrapper.
         */
        @JvmStatic
        fun of(entity: BlockEntity): IBlockEntity = entity as? IBlockEntity ?: WrapperBlockEntity(entity)
    }
}

/**
 * Engine seam over [Level].
 */
interface ILevel {
    val isClientSide: Boolean

    val dimension: ResourceKey<Level>

    val dimensionId: Identifier

    fun toMinecraft(): Level

    fun isLoaded(pos: BlockPos): Boolean

    fun getIBlockEntity(pos: BlockPos): IBlockEntity?

    fun setIBlockEntity(entity: IBlockEntity)

    fun setBlockEntity(entity: BlockEntity)

    companion object {
        @JvmStatic
        fun of(level: Level): ILevel = WrapperLevel.of(level)
    }
}
