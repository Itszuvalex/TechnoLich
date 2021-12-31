package com.itszuvalex.technolich.api.storage;

import java.util.stream.IntStream;

class ItemStorageSliceTest extends ItemStorageTestBase {
    @Override
    public IItemStorage storageWithSize(int size) {
        return new ItemStorageSlice(new ItemStorageArray(size * 2), IntStream.rangeClosed(1, size).toArray());
    }
}