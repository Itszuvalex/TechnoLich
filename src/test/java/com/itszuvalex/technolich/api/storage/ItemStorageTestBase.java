package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.MCAssert;
import com.itszuvalex.technolich.TestableIItemStack;
import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

public abstract class ItemStorageTestBase {

    @BeforeAll
    public static void ClassSetup() {
        TestableIItemStack.overrideNBTSerializer();
    }

    @AfterAll
    public static void ClassTeardown() {
        TestableIItemStack.resetNBTSerializer();
    }

    public abstract IItemStorage storageWithSize(int size);

    class TestState {
        public static int SIZE = 10;
        public TestableIItemStack item0;
        public TestableIItemStack item1;
        public TestableIItemStack item2;
        public TestableIItemStack item3;
        public TestableIItemStack item4;
        public TestableIItemStack item5;
        public TestableIItemStack item6;
        public IItemStorage storage;

        public TestState() {
            storage = storageWithSize(SIZE);
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

        public IItemStorage storage() {
            return storage;
        }

        public int testLength() {
            return SIZE;
        }
    }


    public TestState getState() {
        return new TestState();
    }

    @Test
    void SetSlot_SetsSlot() {
        // Arrange
        var storage = storageWithSize(1);
        MCAssert.assertIItemStackEmpty(storage.get(0));
        // Act
        storage.setSlot(0, new TestableIItemStack(1, 3));
        // Assert
        MCAssert.assertIItemStackNotEmpty(storage.get(0));
        Assertions.assertEquals(3, storage.get(0).stackSize());
    }

    @Test
    void Size_Always_EqualsArrayLength() {
        var state = getState();
        // Assert
        Assertions.assertEquals(state.testLength(), state.storage().size());
    }

    @Test
    void Split_OnEmpty_ReturnEmpty() {
        var state = getState();
        // Act
        var ret = state.storage().split(7, 1);
        // Assert
        MCAssert.assertIItemStackEmpty(ret);
    }

    @Test
    void Split_OnLessThanStack_ModifiesAndReturns() {
        var state = getState();
        // Act
        var ret = state.storage().split(1, 2);
        // Assert
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertEquals(2, ret.stackSize());
        var contents = state.storage().get(1);
        MCAssert.assertIItemStackNotEmpty(contents);
        Assertions.assertEquals(3, contents.stackSize());
    }

    @Test
    void Split_OnExactStack_ModifiesEmptiesAndReturns() {
        var state = getState();
        // Act
        var ret = state.storage().split(1, 5);
        // Assert
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertEquals(ret.stackSize(), 5);
        MCAssert.assertIItemStackEmpty(state.storage().get(1));
    }

    @Test
    void Split_OnMore_ModifiesEmptiesAndReturnsContents() {
        var state = getState();
        // Act
        var ret = state.storage().split(1, 10);
        // Assert
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertEquals(ret.stackSize(), 5);
        MCAssert.assertIItemStackEmpty(state.storage().get(1));
    }

    @Test
    void Insert_EmptyItemStack_ModifyNothingReturnEmpty() {
        var state = getState();
        // Arrange
        var ins = IItemStack.Empty;
        var copy = state.storage().get(1).copy();
        Assertions.assertNotSame(copy, state.storage().get(1));
        // Act
        MCAssert.assertIItemStackEmpty(state.storage().insert(1, ins));
        // Assert
        Assertions.assertTrue(copy.isItemEqual(state.storage().get(1)));
    }

    @Test
    void Insert_ItemIntoEmpty_InsertWholeItemReturnEmpty() {
        var state = getState();
        // Arrange
        var ins = new TestableIItemStack(1, 2);
        MCAssert.assertIItemStackEmpty(state.storage().get(9));
        // Act
        MCAssert.assertIItemStackEmpty(state.storage().insert(9, ins));
        // Assert
        Assertions.assertTrue(state.storage().get(9).isItemEqual(ins));
        Assertions.assertEquals(state.storage().get(9).stackSize(), ins.stackSize());
    }

    @Test
    void Insert_ItemIntoMatchingItemStackWithRoom_ModifyReturnEmpty() {
        var state = getState();
        // Arrange
        var ins = new TestableIItemStack(1, 3);
        var cur = state.storage().get(1);
        Assertions.assertEquals(5, cur.stackSize());
        Assertions.assertTrue(ins.isItemEqual(cur));
        // Act
        MCAssert.assertIItemStackEmpty(state.storage().insert(1, ins));
        //Assert
        Assertions.assertEquals(8, state.storage().get(1).stackSize());
    }

    @Test
    void Insert_ItemIntoMatchingItemLimitedRoom_FillReturnRemains() {
        var state = getState();
        // Arrange
        var ins = new TestableIItemStack(1, 63);
        var cur = state.storage().get(1);
        Assertions.assertEquals(5, cur.stackSize());
        Assertions.assertTrue(ins.isItemEqual(cur));
        // Act
        var ret = state.storage().insert(1, ins);
        // Assert
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertTrue(ins.isItemEqual(ret));
        MCAssert.assertIItemStackNotEmpty(state.storage().get(1));
        Assertions.assertEquals(64, state.storage().get(1).stackSize());
        Assertions.assertEquals(4, ret.stackSize());
    }

    @Test
    void Insert_ItemIntoSlotWithLimitedRoom_FillUntilMaxReturnRemaining() {
        var state = getState();
        // Arrange
        var ins = new TestableIItemStack(1, 200);
        var cur = state.storage().get(9);
        MCAssert.assertIItemStackEmpty(cur);
        // Act
        var ret = state.storage().insert(9, ins);
        // Assert
        Assertions.assertTrue(ret.isItemEqual(ins));
        Assertions.assertEquals(200 - 64, ret.stackSize());
        Assertions.assertTrue(state.storage().get(9).isItemEqual(ins));
        Assertions.assertEquals(state.storage().get(9).stackSize(), state.storage().maxStackSize(9));
    }

    @Test
    void Insert_StackIntoSlotContainingDifferentItem_ReturnInsertModifyNothing() {
        var state = getState();
        // Arrange
        var ins = new TestableIItemStack(3, 20);
        var slotCopy = state.storage().get(1).copy();
        // Act
        var ret = state.storage().insert(1, ins);
        // Assert
        Assertions.assertTrue(ret.isItemEqual(ins));
        Assertions.assertEquals(ret.stackSize(), ins.stackSize());
        Assertions.assertTrue(slotCopy.isItemEqual(state.storage().get(1)));
        Assertions.assertEquals(slotCopy.stackSize(), state.storage().get(1).stackSize());
    }

    // Doesn't use state

    @Test
    void TransferIntoStorage_OneStackInFirstRoomInSecond_EmptyFirstInventoryInsertIntoSecond() {
        // Arrange
        var emptyitem = new TestableIItemStack(1, 1);
        var emptying = storageWithSize(1);
        emptying.setSlot(0, emptyitem);
        var filling = storageWithSize(1);
        filling.setSlot(0, IItemStack.Empty);
        // Act
        var ret = emptying.transferIntoStorage(filling, 1);
        // Assert
        MCAssert.assertIItemStackEmpty(emptying.get(0));
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(1, filling.get(0).stackSize());
        Assertions.assertEquals(0, ret);
    }

    @Test
    void TransferIntoStorage_TransferSomeOfEntireStack_ReduceFirstInsertSecond() {
        // Arrange
        var emptyitem = new TestableIItemStack(1, 2);
        var emptying = storageWithSize(1);
        emptying.setSlot(0, emptyitem);
        var filling = storageWithSize(1);
        filling.setSlot(0, IItemStack.Empty);
        // Act
        var ret = emptying.transferIntoStorage(filling, 1);
        // Assert
        MCAssert.assertIItemStackNotEmpty(emptying.get(0));
        Assertions.assertEquals(1, emptying.get(0).stackSize());
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(1, filling.get(0).stackSize());
        Assertions.assertEquals(0, ret);
    }

    @Test
    void TransferIntoStorage_TransferRequestMoreThanExists_EmptyFirstInsertSecondReturnRemaining() {
        // Arrange
        var emptyitem = new TestableIItemStack(1, 1);
        var emptying = storageWithSize(1);
        emptying.setSlot(0, emptyitem);
        var filling = storageWithSize(1);
        filling.setSlot(0, IItemStack.Empty);
        // Act
        var ret = emptying.transferIntoStorage(filling, 2);
        // Assert
        MCAssert.assertIItemStackEmpty(emptying.get(0));
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(1, filling.get(0).stackSize());
        Assertions.assertEquals(1, ret);
    }

    @Test
    void TransferIntoStorage_MultipleOfItemIntoMostlyFilledStack_OverflowIntoSecondSlot() {
        // Arrange
        var emptyitem = new TestableIItemStack(1, 5);
        var emptying = storageWithSize(1);
        emptying.setSlot(0, emptyitem);
        var filling = storageWithSize(2);
        filling.setSlot(0, new TestableIItemStack(1, 63));
        filling.setSlot(1, new TestableIItemStack(1, 1));
        // Act
        var ret = emptying.transferIntoStorage(filling, 2);
        // Assert
        MCAssert.assertIItemStackNotEmpty(emptying.get(0));
        Assertions.assertEquals(3, emptying.get(0).stackSize());
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        MCAssert.assertIItemStackNotEmpty(filling.get(1));
        Assertions.assertEquals(64, filling.get(0).stackSize());
        Assertions.assertEquals(2, filling.get(1).stackSize());
        Assertions.assertEquals(0, ret);
    }

    @Test
    void TransferIntoStorage_TransferMultipleTypesOfItems() {
        // Arrange
        var emptying = storageWithSize(2);
        var emptyitem0 = new TestableIItemStack(0);
        var emptyitem1 = new TestableIItemStack(1);
        emptying.setSlot(0, emptyitem0);
        emptying.setSlot(1, emptyitem1);
        var filling = storageWithSize(2);
        filling.setSlot(0, IItemStack.Empty);
        filling.setSlot(1, IItemStack.Empty);
        // Act
        var ret = emptying.transferIntoStorage(filling, 2);
        // Assert
        MCAssert.assertIItemStackEmpty(emptying.get(0));
        MCAssert.assertIItemStackEmpty(emptying.get(1));
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(1, filling.get(0).stackSize());
        Assertions.assertEquals(0, ((TestableIItemStack) filling.get(0)).testItem);
        MCAssert.assertIItemStackNotEmpty(filling.get(1));
        Assertions.assertEquals(1, filling.get(1).stackSize());
        Assertions.assertEquals(1, ((TestableIItemStack) filling.get(1)).testItem);
        Assertions.assertEquals(0, ret);
    }

    @Test
    void TransferIntoStorage_MergeItemsTogether() {
        // Arrange
        var emptying = storageWithSize(2);
        var emptyitem0 = new TestableIItemStack(1);
        var emptyitem1 = new TestableIItemStack(1);
        emptying.setSlot(0, emptyitem0);
        emptying.setSlot(1, emptyitem1);
        var filling = storageWithSize(1);
        filling.setSlot(0, IItemStack.Empty);
        // Act
        var ret = emptying.transferIntoStorage(filling, 2);
        // Assert
        MCAssert.assertIItemStackEmpty(emptying.get(0));
        MCAssert.assertIItemStackEmpty(emptying.get(1));
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(2, filling.get(0).stackSize());
        Assertions.assertEquals(0, ret);
    }

    @Test
    void TransferIntoStorage_PrioritizeMatchingOverEmptySlot() {
        // Arrange
        var emptying = storageWithSize(1);
        var emptyitem = new TestableIItemStack(1, 1);
        emptying.setSlot(0, emptyitem);
        var filling = storageWithSize(2);
        filling.setSlot(0, IItemStack.Empty);
        filling.setSlot(1, new TestableIItemStack(1, 1));
        // Act
        var ret = emptying.transferIntoStorage(filling, 1);
        // Assert
        MCAssert.assertIItemStackEmpty(emptying.get(0));
        MCAssert.assertIItemStackEmpty(filling.get(0));
        MCAssert.assertIItemStackNotEmpty(filling.get(1));
        Assertions.assertEquals(2, filling.get(1).stackSize());
        Assertions.assertEquals(0, ret);
    }

    @Test
    void TransferIntoStorage_MoveNothingIfNoRoom() {
        // Arrange
        var emptying = storageWithSize(1);
        var emptyitem = new TestableIItemStack(1, 1);
        emptying.setSlot(0, emptyitem);
        var filling = storageWithSize(1);
        filling.setSlot(0, new TestableIItemStack(2, 1));
        // Act
        var ret = emptying.transferIntoStorage(filling, 1);
        // Assert
        MCAssert.assertIItemStackNotEmpty(emptying.get(0));
        Assertions.assertEquals(1, emptying.get(0).stackSize());
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(1, filling.get(0).stackSize());
        Assertions.assertEquals(1, ret);
    }

    @Test
    void SerializeDeserializeNBT_ShouldWork() {
        var state = getState();
        // Arrange
        var storage2 = new ItemStorageArray(state.testLength());
        IntStream.range(0, storage2.size()).mapToObj(storage2::get).forEach(MCAssert::assertIItemStackEmpty);
        var nbt = state.storage().serializeNBT();
        // Act
        storage2.deserializeNBT(nbt);
        IntStream.range(0, state.storage().size()).forEach((i) -> {
            Assertions.assertEquals(
                    state.storage().get(i).isEmpty(),
                    storage2.get(i).isEmpty());
            if (!state.storage().get(i).isEmpty()) {
                Assertions.assertEquals(((TestableIItemStack) state.storage().get(i)).testItem,
                        ((TestableIItemStack) storage2.get(i)).testItem);
                Assertions.assertEquals(state.storage().get(i).stackSize(),
                        storage2.get(i).stackSize());
                Assertions.assertEquals(state.storage().get(i).damage(),
                        storage2.get(i).damage());
                Assertions.assertNotSame(state.storage().get(i),
                        storage2.get(i));
            }
        });
    }
}
