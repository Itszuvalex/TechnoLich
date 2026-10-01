package com.itszuvalex.technolich.core.frag

import com.itszuvalex.technolich.api.Modules
import com.itszuvalex.technolich.api.adapters.IBlockEntity
import com.itszuvalex.technolich.api.adapters.IColorable
import com.itszuvalex.technolich.api.adapters.IItemStack
import com.itszuvalex.technolich.api.adapters.ILevel
import com.itszuvalex.technolich.api.adapters.IModule
import com.itszuvalex.technolich.api.storage.IItemStorage
import com.itszuvalex.technolich.api.utility.NBTSerializationScope
import com.itszuvalex.technolich.util.Color
import com.itszuvalex.technolich.util.InventoryUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * Gives a block entity a color, exposed on every side through [Modules.COLORABLE]. Setting it saves and syncs to
 * clients. Persisted in every scope as `color` (packed ARGB int).
 */
class FragColorable @JvmOverloads constructor(initial: Color = Color.TRANSPARENT) :
    BlockEntityFragment<IColorable>(), IColorable {
    private var current: Color = initial

    override var color: Color
        get() = current
        set(value) {
            if (value == current) return
            current = value
            markDirtyAndSync()
        }

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.putInt(COLOR_TAG, current.toInt())

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        // Loading is not a change: no dirty mark or sync.
        current = Color(input.getIntOr(COLOR_TAG, 0))
    }

    override fun handlesScope(scope: NBTSerializationScope): Boolean = true

    override val module: IModule<IColorable> get() = Modules.COLORABLE

    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IColorable? = { this }

    override val name: String get() = "Colorable"

    companion object {
        const val COLOR_TAG = "color"
    }
}

/**
 * Drops [storage]'s contents when the block is removed (server side, only when the block actually changes), then
 * clears it.
 *
 * @param drop Drops one stack into the world; replaceable for tests.
 */
class FragDropInventory @JvmOverloads constructor(
    private val storage: IItemStorage,
    private val drop: (ILevel, BlockPos, IItemStack) -> Unit = InventoryUtils::dropItem,
) : InternalBlockEntityFragment() {
    @JvmField
    var shouldDrop = true

    override val name: String get() = NAME

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        if (!shouldDrop) return
        for (i in 0 until storage.size) {
            drop(level, pos, storage.get(i))
            storage.setSlot(i, IItemStack.Empty)
        }
    }

    companion object {
        const val NAME = "Drop Inventory"
    }
}
