package com.itszuvalex.technolich.dev;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.api.ModuleCapabilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Development-only content for exercising the framework in-game and in game tests.  Never registered in production.
 */
public final class DevContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechnoLich.NAMELOWER);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TechnoLich.NAMELOWER);

    public static final DeferredBlock<DevFragBlock> DEV_FRAG_BLOCK = BLOCKS.registerBlock("dev_frag_block", DevFragBlock::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DevFragBlockEntity>> DEV_FRAG_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("dev_frag_block", () -> new BlockEntityType<>(DevFragBlockEntity::new, DEV_FRAG_BLOCK.get()));

    private DevContent() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        DevGameTests.register(modEventBus);
        modEventBus.addListener(DevContent::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ModuleCapabilities.registerBlockEntity(event, DEV_FRAG_BLOCK_ENTITY.get());
    }
}
