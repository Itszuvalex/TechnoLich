package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.IScopedNBTSerialization;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IInternalBlockEntityFragment extends IBlockEntityEventHandler, IScopedNBTSerialization<CompoundTag> {
    @NotNull
    @Nonnull
    String name();

}
