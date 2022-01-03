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

public class ModuleCapabilityArrayListMap implements IMutableModuleCapabilityMap {
    private record ModulePair(IModule<?> mod, Function<Direction, LazyOptional<?>> provider) {
    }

    private record CapPair(Capability<?> cap, Function<Direction, LazyOptional<?>> provider) {
    }

    private final @Nonnull
    @NotNull ArrayList<ModulePair> modList = new ArrayList<>();
    private final @Nonnull
    @NotNull ArrayList<CapPair> capList = new ArrayList<>();

    @Override
    public void addModule(@NotNull @Nonnull IModule<?> module,
                          @NotNull @Nonnull Function<Direction, LazyOptional<?>> provider) {
        modList.add(new ModulePair(module, provider));
        module.capability().ifPresent((c) -> capList.add(new CapPair(c, provider)));
    }

    @Override
    public void addCapability(@NotNull @Nonnull Capability<?> cap, @NotNull @Nonnull Function<Direction,
            LazyOptional<?>> provider) {
        capList.add(new CapPair(cap, provider));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        var matching = modList.stream().filter((p) -> p.mod == module).findFirst();
        if (matching.isEmpty()) return LazyOptional.empty();
        return matching.get().provider.apply(side).cast();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        var matching = capList.stream().filter((p) -> p.cap == cap).findFirst();
        if (matching.isEmpty()) return LazyOptional.empty();
        return matching.get().provider.apply(side).cast();
    }
}
