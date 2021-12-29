package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.TestableIItemStack;
import com.itszuvalex.technolich.api.adapters.IItemStack;
import net.minecraft.nbt.CompoundTag;

public class ItemStorageNBTTest extends ItemStorageTestBase {

    @Override
    public ITestState getState() {
        return new NBTTestState();
    }

    class NBTTestState implements ITestState {
        public static int SIZE = 10;
        public TestableIItemStack item0;
        public TestableIItemStack item1;
        public TestableIItemStack item2;
        public TestableIItemStack item3;
        public TestableIItemStack item4;
        public TestableIItemStack item5;
        public TestableIItemStack item6;
        public ItemStorageNBT storage;

        public NBTTestState() {
            var nbt = new CompoundTag();
            storage = new ItemStorageNBT(nbt, SIZE);
            item0 = new TestableIItemStack(1, 1);
            item1 = new TestableIItemStack(1, 5);
            item2 = new TestableIItemStack(1, 10);
            item3 = new TestableIItemStack(1, 1, 1);
            item4 = new TestableIItemStack(1, 2, 2);
            item5 = new TestableIItemStack(2);
            item6 = new TestableIItemStack(2, 3, 1);
            storage.setSlot(0, item0);
            storage.setSlot(1, item1);
            storage.setSlot(2, item2);
            storage.setSlot(3, item3);
            storage.setSlot(4, item4);
            storage.setSlot(5, item5);
            storage.setSlot(6, item6);
        }

        @Override
        public IItemStorage storage() {
            return storage;
        }

        @Override
        public int testLength() {
            return SIZE;
        }
    }
}
