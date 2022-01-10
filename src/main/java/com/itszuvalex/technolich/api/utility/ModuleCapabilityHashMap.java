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
import java.util.function.Supplier;

public class ModuleCapabilityHashMap implements IMutableModuleCapabilityMap {
    private boolean valid = true;
    private final @Nonnull
    @NotNull HashMap<IModule<?>, Object> modMap = new HashMap<>();
    private final @Nonnull
    @NotNull HashMap<Capability<?>, Object> capMap = new HashMap<>();

    @Override
    public <T> void addModule(@NotNull @Nonnull IModule<T> module,
                              @NotNull @Nonnull Function<Direction, Supplier<LazyOptional<T>>> provider) {
        modMap.put(module, provider);
        module.capability().ifPresent((c) -> capMap.put(c, provider));
    }

    @Override
    public <T> void addCapability(@NotNull @Nonnull Capability<T> cap, @NotNull @Nonnull Function<Direction,
            Supplier<LazyOptional<T>>> provider) {
        capMap.put(cap, provider);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        if(!valid) return LazyOptional.empty();

        var funcObj = modMap.get(module);
        if (funcObj == null) return LazyOptional.empty();
        var func = (Function<Direction, Supplier<LazyOptional<T>>>) funcObj;
        return func.apply(side).get().cast();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(!valid) return LazyOptional.empty();

        var funcObj = capMap.get(cap);
        if (funcObj == null) return LazyOptional.empty();
        var func = (Function<Direction, Supplier<LazyOptional<T>>>) funcObj;
        return func.apply(side).get().cast();
    }

    @Override
    public void invalidateFrags() {
        valid = false;
    }

    @Override
    public void rehydrateFrags() {
        valid = true;
    }

}
