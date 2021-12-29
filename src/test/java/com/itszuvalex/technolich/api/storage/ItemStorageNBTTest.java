package com.itszuvalex.technolich.api.storage;

import net.minecraft.nbt.CompoundTag;

public class ItemStorageNBTTest extends ItemStorageTestBase {
    @Override
    public IItemStorage storageWithSize(int size) {
        return new ItemStorageNBT(new CompoundTag(), size);
    }
}
