package com.itszuvalex.technolich.api.utility;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IScopedNBTSerialization<T> {
    @NotNull
    @Nonnull
    T serialize(NBTSerializationScope scope);

    void deserialize(@NotNull @Nonnull T nbt, NBTSerializationScope scope);

    boolean handlesScope(NBTSerializationScope scope);

}
