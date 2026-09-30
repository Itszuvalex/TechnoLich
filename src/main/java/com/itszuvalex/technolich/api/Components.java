package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.TechnoLich;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * TechnoLich data component types.
 */
public final class Components {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TechnoLich.NAMELOWER);

    /**
     * ITEM-scope fragment data of a {@link com.itszuvalex.technolich.core.BlockEntityCore}, carried by its item form.
     * Written by {@code collectImplicitComponents} (loot {@code copy_components} from the block entity, creative
     * pick-block) and applied back on placement.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> FRAGMENT_DATA =
            DATA_COMPONENTS.registerComponentType("fragment_data",
                    (builder) -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));

    private Components() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
