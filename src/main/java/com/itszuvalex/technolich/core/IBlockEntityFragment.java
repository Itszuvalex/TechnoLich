package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.api.utility.IScopedNBTSerialization;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

public interface IBlockEntityFragment<T> extends IScopedNBTSerialization<CompoundTag> {
    @NotNull
    @Nonnull
    String name();

    @NotNull
    @Nonnull
    Module<T> module();

    @NotNull
    @Nonnull
    Function<Direction, Optional<T>> faceToModuleMapper();
}
