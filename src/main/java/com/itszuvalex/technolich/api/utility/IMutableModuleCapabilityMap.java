package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Providers map a nullable side to the object exposed on that side, or null if nothing is exposed there.
 */
public interface IMutableModuleCapabilityMap extends IModuleCapabilityMap {
    <T> void addModule(@NotNull @Nonnull IModule<T> module,
                       @NotNull @Nonnull Function<Direction, T> provider);

    <T> void addCapability(@NotNull @Nonnull BlockCapability<T, Direction> cap,
                           @NotNull @Nonnull Function<Direction, T> provider);
}
