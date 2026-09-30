package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public class ModuleCapabilityHashMap implements IMutableModuleCapabilityMap {
    private boolean valid = true;
    private final @Nonnull
    @NotNull HashMap<IModule<?>, Function<Direction, ?>> modMap = new HashMap<>();
    private final @Nonnull
    @NotNull HashMap<BlockCapability<?, Direction>, Function<Direction, ?>> capMap = new HashMap<>();

    @Override
    public <T> void addModule(@NotNull @Nonnull IModule<T> module,
                              @NotNull @Nonnull Function<Direction, T> provider) {
        modMap.put(module, provider);
        module.blockCapability().ifPresent((c) -> capMap.put(c, provider));
    }

    @Override
    public <T> void addCapability(@NotNull @Nonnull BlockCapability<T, Direction> cap,
                                  @NotNull @Nonnull Function<Direction, T> provider) {
        capMap.put(cap, provider);
    }

    @Override
    public @NotNull <T> Optional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        if (!valid) return Optional.empty();

        var func = (Function<Direction, T>) modMap.get(module);
        if (func == null) return Optional.empty();
        return Optional.ofNullable(func.apply(side));
    }

    @Override
    public <T> @Nullable T getCapability(@NotNull BlockCapability<T, Direction> cap, @Nullable Direction side) {
        if (!valid) return null;

        var func = (Function<Direction, T>) capMap.get(cap);
        if (func == null) return null;
        return func.apply(side);
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
