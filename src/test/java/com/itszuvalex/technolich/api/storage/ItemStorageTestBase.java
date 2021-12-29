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

    interface ITestState {
        IItemStorage storage();

        int testLength();
    }


    public abstract ITestState getState();

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
