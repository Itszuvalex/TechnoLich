package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.storage.IItemStorage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SidedItemStorageConfiguration extends SidedStorageConfiguration<IItemStorage> {
    public SidedItemStorageConfiguration(@NotNull Function<Direction, String> defaults, @NotNull Map<String, IItemStorage> storages, @NotNull Supplier<Direction> front) {
        super(defaults, storages, front);
    }
}
