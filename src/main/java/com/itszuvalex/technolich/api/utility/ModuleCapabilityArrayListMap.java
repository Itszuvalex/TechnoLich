package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public class ModuleCapabilityArrayListMap implements IMutableModuleCapabilityMap {
    private boolean valid = true;

    private record ModulePair<T>(IModule<T> mod, Function<Direction, T> provider) {
    }

    private record CapPair<T>(BlockCapability<T, Direction> cap, Function<Direction, T> provider) {
    }

    private final @Nonnull
    @NotNull ArrayList<ModulePair<?>> modList = new ArrayList<>();
    private final @Nonnull
    @NotNull ArrayList<CapPair<?>> capList = new ArrayList<>();

    @Override
    public <T> void addModule(@NotNull @Nonnull IModule<T> module,
                              @NotNull @Nonnull Function<Direction, T> provider) {
        modList.add(new ModulePair<>(module, provider));
        module.blockCapability().ifPresent((c) -> capList.add(new CapPair<>(c, provider)));
    }

    @Override
    public <T> void addCapability(@NotNull @Nonnull BlockCapability<T, Direction> cap,
                                  @NotNull @Nonnull Function<Direction, T> provider) {
        capList.add(new CapPair<>(cap, provider));
    }

    @Override
    public @NotNull <T> Optional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        if (!valid) return Optional.empty();

        for (var pair : modList) {
            if (pair.mod == module) return Optional.ofNullable(((ModulePair<T>) pair).provider.apply(side));
        }
        return Optional.empty();
    }

    @Override
    public <T> @Nullable T getCapability(@NotNull BlockCapability<T, Direction> cap, @Nullable Direction side) {
        if (!valid) return null;

        for (var pair : capList) {
            if (pair.cap == cap) return ((CapPair<T>) pair).provider.apply(side);
        }
        return null;
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
