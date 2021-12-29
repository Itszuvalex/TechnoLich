package com.itszuvalex.technolich.api.utility;

import net.minecraft.nbt.Tag;

public interface INBTObjectSerializer<T, N extends Tag> {
    void serialize(T obj, N tag);
    T deserialize(N tag);
}
