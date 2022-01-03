package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface IMutableModuleCapabilityMap extends IModuleCapabilityMap {
    void addModule(@NotNull @Nonnull IModule<?> module,
                   @NotNull @Nonnull Function<Direction, LazyOptional<?>> provider);

    void addCapability(@NotNull @Nonnull Capability<?> cap, @NotNull @Nonnull Function<Direction,
            LazyOptional<?>> provider);
}
