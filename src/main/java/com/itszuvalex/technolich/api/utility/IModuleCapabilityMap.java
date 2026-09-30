package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModuleProvider;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public interface IModuleCapabilityMap extends IModuleProvider {
    /**
     * @return The capability instance for the given side, or null if not provided.  Suitable for returning directly
     * from a NeoForge capability provider.
     */
    <T> @Nullable T getCapability(@NotNull @Nonnull BlockCapability<T, Direction> cap, @Nullable Direction side);

    void invalidateFrags();

    void rehydrateFrags();
}
