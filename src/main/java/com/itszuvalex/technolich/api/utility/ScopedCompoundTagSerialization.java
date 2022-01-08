package com.itszuvalex.technolich.api.utility;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface ScopedCompoundTagSerialization extends IScopedNBTSerialization<CompoundTag> {
    @Override
    @NotNull default CompoundTag serialize(NBTSerializationScope scope) {
        CompoundTag tag = new CompoundTag();
        serializeTo(scope, tag);
        return tag;
    }
}
