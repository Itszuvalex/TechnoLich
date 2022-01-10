package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModuleCapabilityArrayListMap implements IMutableModuleCapabilityMap {
    private boolean valid = true;

    private record ModulePair<T>(IModule<T> mod, Function<Direction, Supplier<LazyOptional<T>>> provider) {
    }

    private record CapPair<T>(Capability<T> cap, Function<Direction, Supplier<LazyOptional<T>>> provider) {
    }

    private final @Nonnull
    @NotNull ArrayList<ModulePair<?>> modList = new ArrayList<>();
    private final @Nonnull
    @NotNull ArrayList<CapPair<?>> capList = new ArrayList<>();

    @Override
    public <T> void addModule(@NotNull @Nonnull IModule<T> module,
                              @NotNull @Nonnull Function<Direction, Supplier<LazyOptional<T>>> provider) {
        modList.add(new ModulePair<>(module, provider));
        module.capability().ifPresent((c) -> capList.add(new CapPair<>(c, provider)));
    }

    @Override
    public <T> void addCapability(@NotNull @Nonnull Capability<T> cap, @NotNull @Nonnull Function<Direction,
            Supplier<LazyOptional<T>>> provider) {
        capList.add(new CapPair<>(cap, provider));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        if (!valid) return LazyOptional.empty();

        var matching = modList.stream().filter((p) -> p.mod == module).findFirst();
        if (matching.isEmpty()) return LazyOptional.empty();
        return matching.get().provider.apply(side).get().cast();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!valid) return LazyOptional.empty();

        var matching = capList.stream().filter((p) -> p.cap == cap).findFirst();
        if (matching.isEmpty()) return LazyOptional.empty();
        return matching.get().provider.apply(side).get().cast();
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
