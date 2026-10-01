package com.itszuvalex.technolich.api.utility

import com.itszuvalex.technolich.api.adapters.IBlockEntity
import com.itszuvalex.technolich.api.adapters.ILevel
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.world.level.Level
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A block position in a dimension. A plain value: equality, hashing and ordering are by dimension and position. It
 * holds no reference to a level; to see what is there, pass the level to [getIBlockEntity].
 */
data class Loc4(val dimensionId: Identifier, val pos: BlockPos) : Comparable<Loc4> {
    val x: Int get() = pos.x
    val y: Int get() = pos.y
    val z: Int get() = pos.z

    val chunkCoords: ChunkCoord get() = ChunkCoord.of(pos)

    fun getOffset(x: Int, y: Int, z: Int): Loc4 = copy(pos = pos.offset(x, y, z))

    fun getOffset(offset: BlockPos): Loc4 = copy(pos = pos.offset(offset))

    /**
     * @param level The level for [dimensionId].
     * @param force Look up even if the chunk is not loaded (loads it).
     * @throws IllegalArgumentException if [level] is a different dimension.
     */
    @JvmOverloads
    fun getIBlockEntity(level: ILevel, force: Boolean = false): IBlockEntity? {
        require(level.dimensionId == dimensionId) { "$this looked up in ${level.dimensionId}" }
        return if (force || level.isLoaded(pos)) level.getIBlockEntity(pos) else null
    }

    /**
     * @return [Double.MAX_VALUE] across dimensions.
     */
    fun distSqr(other: Loc4): Double {
        if (other.dimensionId != dimensionId) return Double.MAX_VALUE
        return distSqr(other.x, other.y, other.z)
    }

    fun distSqr(x: Int, y: Int, z: Int): Double {
        val dx = (this.x - x).toDouble()
        val dy = (this.y - y).toDouble()
        val dz = (this.z - z).toDouble()
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * @return [Double.MAX_VALUE] across dimensions.
     */
    fun dist(other: Loc4): Double {
        if (other.dimensionId != dimensionId) return Double.MAX_VALUE
        return dist(other.x, other.y, other.z)
    }

    fun dist(x: Int, y: Int, z: Int): Double = sqrt(distSqr(x, y, z))

    override fun compareTo(other: Loc4): Int = compareValuesBy(this, other, Loc4::dimensionId, Loc4::x, Loc4::y, Loc4::z)

    override fun toString(): String = "Loc4[$dimensionId ${pos.x},${pos.y},${pos.z}]"

    fun isNeighbor(loc: Loc4): Boolean {
        if (dimensionId != loc.dimensionId) return false
        if (abs(x - loc.x) == 1 && y == loc.y && z == loc.z) return true
        if (x == loc.x && abs(y - loc.y) == 1 && z == loc.z) return true
        return x == loc.x && y == loc.y && abs(z - loc.z) == 1
    }

    companion object {
        @JvmStatic
        fun of(level: Level, pos: BlockPos): Loc4 = Loc4(level.dimension().identifier(), pos)

        @JvmStatic
        fun of(level: ILevel, pos: BlockPos): Loc4 = Loc4(level.dimensionId, pos)

        const val X_KEY = "x"
        const val Y_KEY = "y"
        const val Z_KEY = "z"
        const val DIM_KEY = "dim"

        @JvmField
        val ORIGIN: Loc4 = Loc4(Identifier.parse("overworld"), BlockPos(0, 0, 0))

        @JvmField
        val CODEC: Codec<Loc4> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf(X_KEY).forGetter(Loc4::x),
                Codec.INT.fieldOf(Y_KEY).forGetter(Loc4::y),
                Codec.INT.fieldOf(Z_KEY).forGetter(Loc4::z),
                Identifier.CODEC.fieldOf(DIM_KEY).forGetter(Loc4::dimensionId),
            ).apply(instance) { x, y, z, dim -> Loc4(dim, BlockPos(x, y, z)) }
        }
    }
}
