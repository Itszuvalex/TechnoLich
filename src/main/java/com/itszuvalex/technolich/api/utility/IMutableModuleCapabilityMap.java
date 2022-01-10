package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IMutableModuleCapabilityMap extends IModuleCapabilityMap {
    <T> void addModule(@NotNull @Nonnull IModule<T> module,
                   @NotNull @Nonnull Function<Direction, Supplier<LazyOptional<T>>> provider);

    <T> void addCapability(@NotNull @Nonnull Capability<T> cap, @NotNull @Nonnull Function<Direction,
            Supplier<LazyOptional<T>>> provider);
}
