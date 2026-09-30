package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import java.util.Optional;

public interface IModule<T> {
    /**
     * @return Block capability this module is exposed through, with a nullable {@link Direction} side context.
     */
    Optional<BlockCapability<T, Direction>> blockCapability();

    /**
     * @return Item capability this module is exposed through on item stacks.
     */
    Optional<ItemCapability<T, ItemAccess>> itemCapability();

    /**
     * @return Unique, namespaced id (e.g. {@code technolich:colorable}), so modules from different mods can't collide.
     */
    Identifier id();
}
