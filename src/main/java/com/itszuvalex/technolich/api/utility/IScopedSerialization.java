package com.itszuvalex.technolich.api.utility;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IScopedSerialization {
    void serializeTo(NBTSerializationScope scope, @NotNull @Nonnull ValueOutput output);

    void deserialize(@NotNull @Nonnull ValueInput input, NBTSerializationScope scope);

    boolean handlesScope(NBTSerializationScope scope);
}
