package com.itszuvalex.technolich.util;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.storage.IItemStorage;
import net.minecraft.core.BlockPos;

import java.util.stream.IntStream;

public interface IInventoryUtils {
    Singleton<IInventoryUtils> instance = new Singleton<>(InventoryUtils::new);

    default void dropStorage(ILevel level, BlockPos pos, IItemStorage storage) {
        IntStream.range(0, storage.size()).forEach((i) -> dropItem(level, pos, storage.get(i)));
    }

    void dropItem(ILevel level, BlockPos pos, IItemStack item);
}
