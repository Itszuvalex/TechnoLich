package com.itszuvalex.technolich.dev

import com.itszuvalex.technolich.TechnoLich
import com.itszuvalex.technolich.api.ModuleCapabilities
import com.itszuvalex.technolich.api.storage.IItemStorage
import com.itszuvalex.technolich.api.storage.ItemStorageArray
import com.itszuvalex.technolich.api.utility.NBTSerializationScope
import com.itszuvalex.technolich.api.wrappers.WrapperResourceHandlerIItemStorage
import com.itszuvalex.technolich.core.BlockEntityCore
import com.itszuvalex.technolich.core.EntityBlockCore
import com.itszuvalex.technolich.core.FragMultiblockPart
import com.itszuvalex.technolich.core.MultiblockRoleRef
import com.itszuvalex.technolich.core.MultiblockShape
import com.itszuvalex.technolich.core.frag.FragColorable
import com.itszuvalex.technolich.core.frag.FragDropInventory
import com.itszuvalex.technolich.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

/**
 * Dev-only multiblock shapes. A `core` at the shape's origin plus a `wing` one block east.
 */
object DevShapes {
    @JvmField
    val PAIR: MultiblockShape = MultiblockShape.register(
        Identifier.fromNamespaceAndPath(TechnoLich.ID, "dev_pair"),
        mapOf(BlockPos.ZERO to "core", BlockPos(1, 0, 0) to "wing"),
    )
}

/**
 * Development-only content for exercising the framework in-game and in game tests. Never registered in production.
 */
object DevContent {
    @JvmField
    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(TechnoLich.ID)

    @JvmField
    val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TechnoLich.ID)

    @JvmField
    val DEV_FRAG_BLOCK: DeferredBlock<DevFragBlock> = BLOCKS.registerBlock("dev_frag_block", ::DevFragBlock)

    @JvmField
    val DEV_FRAG_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<DevFragBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("dev_frag_block") { -> BlockEntityType(::DevFragBlockEntity, DEV_FRAG_BLOCK.get()) }

    @JvmField
    val DEV_MULTIBLOCK_CORE_BLOCK: DeferredBlock<DevMultiblockCoreBlock> = BLOCKS.registerBlock("dev_multiblock_core", ::DevMultiblockCoreBlock)

    @JvmField
    val DEV_MULTIBLOCK_CORE_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<DevMultiblockCoreBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("dev_multiblock_core") { -> BlockEntityType(::DevMultiblockCoreBlockEntity, DEV_MULTIBLOCK_CORE_BLOCK.get()) }

    @JvmField
    val DEV_MULTIBLOCK_WING_BLOCK: DeferredBlock<DevMultiblockWingBlock> = BLOCKS.registerBlock("dev_multiblock_wing", ::DevMultiblockWingBlock)

    @JvmField
    val DEV_MULTIBLOCK_WING_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<DevMultiblockWingBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("dev_multiblock_wing") { -> BlockEntityType(::DevMultiblockWingBlockEntity, DEV_MULTIBLOCK_WING_BLOCK.get()) }

    fun register(modBus: IEventBus) {
        DevShapes.PAIR // force shape registration before anything can try to form it
        BLOCKS.register(modBus)
        BLOCK_ENTITY_TYPES.register(modBus)
        DevGameTests.register(modBus)
        modBus.addListener { event: RegisterCapabilitiesEvent ->
            ModuleCapabilities.registerBlockEntity(event, DEV_FRAG_BLOCK_ENTITY.get())
            ModuleCapabilities.registerBlockEntity(event, DEV_MULTIBLOCK_CORE_BLOCK_ENTITY.get())
            ModuleCapabilities.registerBlockEntity(event, DEV_MULTIBLOCK_WING_BLOCK_ENTITY.get())
        }
    }
}

class DevFragBlock(properties: BlockBehaviour.Properties) :
    EntityBlockCore<DevFragBlockEntity>(properties, { DevContent.DEV_FRAG_BLOCK_ENTITY.get() }) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = DevFragBlockEntity(pos, state)
}

/**
 * A colorable block with a one-slot inventory that drops on removal and is exposed to other mods.
 */
class DevFragBlockEntity(pos: BlockPos, state: BlockState) : BlockEntityCore(DevContent.DEV_FRAG_BLOCK_ENTITY.get(), pos, state) {
    @JvmField
    val colorable = FragColorable()

    @JvmField
    val inventory: IItemStorage = ItemStorageArray(1) { markDirty() }

    @JvmField
    val itemHandler = WrapperResourceHandlerIItemStorage.of(inventory)

    init {
        fragList.addFragment(colorable)
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addInternalFragment(object : InternalBlockEntityFragment() {
            override fun name(): String = "Inventory"
            override fun handlesScope(scope: NBTSerializationScope): Boolean = scope == NBTSerializationScope.LEVEL
            override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = inventory.serialize(output)
            override fun deserialize(input: ValueInput, scope: NBTSerializationScope) = inventory.deserialize(input)
        })
        fragList.addCapability(Capabilities.Item.BLOCK) { itemHandler }
    }
}

class DevMultiblockCoreBlock(properties: BlockBehaviour.Properties) :
    EntityBlockCore<DevMultiblockCoreBlockEntity>(properties, { DevContent.DEV_MULTIBLOCK_CORE_BLOCK_ENTITY.get() }) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = DevMultiblockCoreBlockEntity(pos, state)
}

/**
 * Fills the `core` slot of [DevShapes.PAIR].
 */
class DevMultiblockCoreBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntityCore(DevContent.DEV_MULTIBLOCK_CORE_BLOCK_ENTITY.get(), pos, state) {
    @JvmField
    val multiblock = FragMultiblockPart(listOf(MultiblockRoleRef(DevShapes.PAIR, "core")))

    init {
        fragList.addFragment(multiblock)
    }
}

class DevMultiblockWingBlock(properties: BlockBehaviour.Properties) :
    EntityBlockCore<DevMultiblockWingBlockEntity>(properties, { DevContent.DEV_MULTIBLOCK_WING_BLOCK_ENTITY.get() }) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = DevMultiblockWingBlockEntity(pos, state)
}

/**
 * Fills the `wing` slot of [DevShapes.PAIR].
 */
class DevMultiblockWingBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntityCore(DevContent.DEV_MULTIBLOCK_WING_BLOCK_ENTITY.get(), pos, state) {
    @JvmField
    val multiblock = FragMultiblockPart(listOf(MultiblockRoleRef(DevShapes.PAIR, "wing")))

    init {
        fragList.addFragment(multiblock)
    }
}
