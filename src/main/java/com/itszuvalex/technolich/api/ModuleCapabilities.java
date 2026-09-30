package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.core.BlockEntityCore;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Exposes {@link BlockEntityCore} fragments to the NeoForge capability system.
 */
public final class ModuleCapabilities {
    /**
     * NeoForge block capabilities that block entities may expose through
     * {@link com.itszuvalex.technolich.core.BlockEntityFragmentCollection#addCapability}.
     */
    public static final List<BlockCapability<?, Direction>> STANDARD = List.of(
            net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,
            net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK,
            net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK
    );

    private ModuleCapabilities() {
    }

    /**
     * Call from a {@link RegisterCapabilitiesEvent} handler (mod bus) for each {@link BlockEntityCore} type.
     * Registers every module's block capability plus {@link #STANDARD}, all routed to
     * {@link BlockEntityCore#getCapability}.  Modules must already be registered.
     */
    public static void registerBlockEntity(@NotNull @Nonnull RegisterCapabilitiesEvent event,
                                           @NotNull @Nonnull BlockEntityType<? extends BlockEntityCore> type) {
        var caps = new LinkedHashSet<BlockCapability<?, Direction>>();
        Module.modules().forEach((m) -> m.blockCapability().ifPresent(caps::add));
        caps.addAll(STANDARD);
        caps.forEach((cap) -> register(event, cap, type));
    }

    private static <T, B extends BlockEntityCore> void register(RegisterCapabilitiesEvent event,
                                                                BlockCapability<T, Direction> cap,
                                                                BlockEntityType<B> type) {
        event.registerBlockEntity(cap, type, (be, side) -> be.getCapability(cap, side));
    }
}
