package com.itszuvalex.technolich.core

import com.itszuvalex.technolich.api.adapters.IBlockEntity
import com.itszuvalex.technolich.api.adapters.ILevel
import com.itszuvalex.technolich.api.adapters.IModule
import com.itszuvalex.technolich.api.utility.IModuleCapabilityMap
import com.itszuvalex.technolich.api.utility.IMutableModuleCapabilityMap
import com.itszuvalex.technolich.api.utility.IScopedSerialization
import com.itszuvalex.technolich.api.utility.ModuleCapabilityArrayListMap
import com.itszuvalex.technolich.api.utility.NBTSerializationScope
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.capabilities.BlockCapability

/**
 * The block entity lifecycle as fragments see it. Every hook defaults to doing nothing, so a fragment overrides only
 * the ones it needs. [BlockEntityCore] drives these through its [BlockEntityFragmentCollection].
 */
interface IFragmentLifecycle {
    /**
     * Called once the owning block entity is fully attached to a loaded level — both for one freshly placed into an
     * already-loaded chunk, and for one loaded in along with its chunk. See [BlockEntity.onLoad].
     */
    fun onLoad(level: ILevel, pos: BlockPos) {}

    /**
     * Called when the owning block entity's chunk unloads while the block entity itself is not being removed
     * (compare [onRemove], a real break/replace). See [BlockEntity.onChunkUnloaded].
     */
    fun onChunkUnloaded(level: ILevel, pos: BlockPos) {}

    /**
     * Called when a neighbouring block changed (`Block#neighborChanged`; 26.1 no longer says which neighbour). Only
     * blocks extending [EntityBlockCore] forward this.
     */
    fun onNeighborChanged(level: ILevel, pos: BlockPos) {}

    /**
     * Server side, called when the block is actually replaced (not for block state changes that keep the block
     * entity).
     */
    fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {}

    /**
     * The block entity was removed from the level (`setRemoved`); exposed modules stop resolving until [rehydrateFrags].
     */
    fun invalidateFrags() {}

    /**
     * The block entity is back in the level (`clearRemoved`).
     */
    fun rehydrateFrags() {}
}

fun interface IBlockEntityTickable {
    fun tick(level: ILevel, blockPos: BlockPos, blockState: BlockState)
}

/**
 * What a fragment can ask of the block entity that owns it. Handed to each fragment through
 * [IInternalBlockEntityFragment.onAttach] when it is added to a [BlockEntityFragmentCollection].
 */
interface IFragmentHost {
    fun blockEntity(): IBlockEntity

    /**
     * Marks the block entity as changed so it is saved. Call after changing LEVEL-scope state.
     */
    fun markDirty()

    /**
     * [markDirty], and on the server also re-sends DESCRIPTION-scope data to clients tracking the chunk.
     * Call after changing state that clients render or display.
     */
    fun markDirtyAndSync()
}

/**
 * A piece of block entity behaviour with its own lifecycle hooks and serialization, but no exposed module. Only
 * [name] is required: lifecycle hooks default to no-ops and serialization defaults to handling no scope.
 */
interface IInternalBlockEntityFragment : IFragmentLifecycle, IScopedSerialization {
    /**
     * Unique within the block entity; keys this fragment's saved data.
     */
    fun name(): String

    /**
     * Called once when the fragment is added to its block entity's fragment collection.
     */
    fun onAttach(host: IFragmentHost) {}

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {}

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {}

    override fun handlesScope(scope: NBTSerializationScope): Boolean = false
}

/**
 * A fragment that exposes a module [T].
 */
interface IBlockEntityFragment<T : Any> : IInternalBlockEntityFragment {
    fun module(): IModule<T>

    /**
     * @return Maps a nullable side to the module instance exposed on that side, or null if not exposed there.
     * Called on every query, so it should return the fragment's current instance.
     */
    fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> T?
}

/**
 * The fragments, exposed modules/capabilities and tickables of one block entity. Forwards every [IFragmentLifecycle]
 * hook to each fragment, in the order they were added.
 */
class BlockEntityFragmentCollection(private val host: IFragmentHost) :
    IFragmentLifecycle, IScopedSerialization, IModuleCapabilityMap, IBlockEntityTickable {
    private val modCapMap: IMutableModuleCapabilityMap = ModuleCapabilityArrayListMap()
    private val modList = ArrayList<IInternalBlockEntityFragment>()
    private val tickList = ArrayList<IBlockEntityTickable>()
    private val fragmentNames = HashSet<String>()
    private val exposedModules = HashSet<IModule<*>>()

    /**
     * @throws IllegalArgumentException if a fragment with the same [IInternalBlockEntityFragment.name] was already
     * added; names key each fragment's saved data, so duplicates would overwrite each other.
     */
    fun addInternalFragment(fragment: IInternalBlockEntityFragment) {
        require(fragmentNames.add(fragment.name())) { "Duplicate fragment name: ${fragment.name()}" }
        modList.add(fragment)
        fragment.onAttach(host)
    }

    /**
     * @throws IllegalArgumentException if the name is a duplicate, or another fragment already exposes this module.
     */
    fun <F : Any> addFragment(fragment: IBlockEntityFragment<F>) {
        require(fragment.module() !in exposedModules) { "Module ${fragment.module().id} is already exposed by another fragment" }
        addInternalFragment(fragment)
        exposedModules.add(fragment.module())
        modCapMap.addModule(fragment.module(), fragment.faceToModuleMapper(host.blockEntity()))
    }

    /**
     * Exposes a capability that is not tied to a module, e.g. one of `ModuleCapabilities.STANDARD`.
     */
    fun <T : Any> addCapability(cap: BlockCapability<T, Direction?>, provider: (Direction?) -> T?) =
        modCapMap.addCapability(cap, provider)

    fun addTickable(tickable: IBlockEntityTickable) {
        tickList.add(tickable)
    }

    override fun onLoad(level: ILevel, pos: BlockPos) = modList.forEach { it.onLoad(level, pos) }

    override fun onChunkUnloaded(level: ILevel, pos: BlockPos) = modList.forEach { it.onChunkUnloaded(level, pos) }

    override fun onNeighborChanged(level: ILevel, pos: BlockPos) = modList.forEach { it.onNeighborChanged(level, pos) }

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) =
        modList.forEach { it.onRemove(level, pos, blockStatePrev) }

    override fun tick(level: ILevel, blockPos: BlockPos, blockState: BlockState) =
        tickList.forEach { it.tick(level, blockPos, blockState) }

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) =
        modList.filter { it.handlesScope(scope) }.forEach { it.serializeTo(scope, output.child(it.name())) }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) =
        modList.filter { it.handlesScope(scope) }.forEach { frag ->
            input.child(frag.name()).ifPresent { frag.deserialize(it, scope) }
        }

    override fun handlesScope(scope: NBTSerializationScope): Boolean = modList.any { it.handlesScope(scope) }

    override fun <T : Any> getModule(module: IModule<T>, side: Direction?): T? = modCapMap.getModule(module, side)

    override fun <T : Any> getCapability(cap: BlockCapability<T, Direction?>, side: Direction?): T? =
        modCapMap.getCapability(cap, side)

    override fun invalidateFrags() {
        modCapMap.invalidateFrags()
        modList.forEach { it.invalidateFrags() }
    }

    override fun rehydrateFrags() {
        modList.forEach { it.rehydrateFrags() }
        modCapMap.rehydrateFrags()
    }
}
