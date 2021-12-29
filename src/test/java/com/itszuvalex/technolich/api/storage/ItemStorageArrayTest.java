package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.MCAssert;
import com.itszuvalex.technolich.TestableIItemStack;
import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

class ItemStorageArrayTest extends ItemStorageTestBase {
    @Override
    public IItemStorage storageWithSize(int size) {
        return new ItemStorageArray(size);
    }
}