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

    @Test
    void Listener_SetSlotAndInsertNotify_QuietDoesNot() {
        var changes = new int[1];
        var storage = new ItemStorageArray(2, () -> changes[0]++);

        storage.setSlotQuietly(0, new TestableIItemStack(1, 1));
        Assertions.assertEquals(0, changes[0]);

        storage.setSlot(0, new TestableIItemStack(1, 2));
        Assertions.assertEquals(1, changes[0]);

        storage.insert(1, new TestableIItemStack(1, 3));
        Assertions.assertEquals(2, changes[0]);

        storage.setChanged();
        Assertions.assertEquals(3, changes[0]);
    }

    @Test
    void Slice_ForwardsQuietAndSetChanged() {
        var changes = new int[1];
        var slice = new ItemStorageSlice(new ItemStorageArray(2, () -> changes[0]++), new int[]{1});

        slice.setSlotQuietly(0, new TestableIItemStack(1, 1));
        Assertions.assertEquals(0, changes[0]);
        slice.setChanged();
        Assertions.assertEquals(1, changes[0]);
    }
}