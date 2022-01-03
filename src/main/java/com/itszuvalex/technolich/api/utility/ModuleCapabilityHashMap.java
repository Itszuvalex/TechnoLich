package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.function.Function;

public class ModuleCapabilityHashMap implements IMutableModuleCapabilityMap {
    private final @Nonnull
    @NotNull HashMap<IModule<?>, Function<Direction, LazyOptional<?>>> modMap = new HashMap<>();
    private final @Nonnull
    @NotNull HashMap<Capability<?>, Function<Direction, LazyOptional<?>>> capMap = new HashMap<>();

    @Override
    public void addModule(@NotNull @Nonnull IModule<?> module,
                          @NotNull @Nonnull Function<Direction, LazyOptional<?>> provider) {
        modMap.put(module, provider);
        module.capability().ifPresent((c) -> capMap.put(c, provider));
    }

    @Override
    public void addCapability(@NotNull @Nonnull Capability<?> cap, @NotNull @Nonnull Function<Direction,
            LazyOptional<?>> provider) {
        capMap.put(cap, provider);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        var func = modMap.get(module);
        if (func == null) return LazyOptional.empty();
        return func.apply(side).cast();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        var func = capMap.get(cap);
        if (func == null) return LazyOptional.empty();
        return func.apply(side).cast();
    }
}
