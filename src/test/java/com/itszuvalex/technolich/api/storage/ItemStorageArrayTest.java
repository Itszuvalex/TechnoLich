package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.MCAssert;
import com.itszuvalex.technolich.TestableIItemStack;
import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

class ItemStorageArrayTest extends ItemStorageTestBase {
    @Override
    public ITestState getState() {
        return new ArrayTestState();
    }

    class ArrayTestState implements ITestState {
        public static int SIZE = 10;
        public IItemStack[] array;
        public TestableIItemStack item0;
        public TestableIItemStack item1;
        public TestableIItemStack item2;
        public TestableIItemStack item3;
        public TestableIItemStack item4;
        public TestableIItemStack item5;
        public TestableIItemStack item6;
        public ItemStorageArray storage;

        public ArrayTestState() {
            array = new IItemStack[SIZE];
            item0 = new TestableIItemStack(1, 1);
            item1 = new TestableIItemStack(1, 5);
            item2 = new TestableIItemStack(1, 10);
            item3 = new TestableIItemStack(1, 1, 1);
            item4 = new TestableIItemStack(1, 2, 2);
            item5 = new TestableIItemStack(2);
            item6 = new TestableIItemStack(2, 3, 1);
            array[0] = item0;
            array[1] = item1;
            array[2] = item2;
            array[3] = item3;
            array[4] = item4;
            array[5] = item5;
            array[6] = item6;
            storage = new ItemStorageArray(array);
        }

        @Override
        public IItemStorage storage() {
            return storage;
        }

        @Override
        public int testLength() {
            return array.length;
        }
    }

    @Test
    void Get_Always_ReturnUnderlyingElementsOfStorage() {
        var state = getState();
        //Assert
        IntStream.range(0, state.testLength()).forEach((i) ->
                Assertions.assertSame(((ArrayTestState) state).array[i], state.storage().get(i))
        );
    }

    @Test
    void SetSlot_Always_UpdateUnderlyingStorage() {
        int index = 7;
        var state = getState();
        // Act
        state.storage().setSlot(index, new TestableIItemStack(5));
        // Assert
        Assertions.assertSame(((ArrayTestState) state).array[index], state.storage().get(index));
    }


    @Test
    void TransferIntoStorage_OneStackInFirstRoomInSecond_EmptyFirstInventoryInsertIntoSecond() {
        // Arrange
        var emptyarray = new IItemStack[1];
        var emptyitem = new TestableIItemStack(1, 1);
        emptyarray[0] = emptyitem;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[1];
        fillarray[0] = IItemStack.Empty;
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[1];
        var emptyitem = new TestableIItemStack(1, 2);
        emptyarray[0] = emptyitem;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[1];
        fillarray[0] = IItemStack.Empty;
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[1];
        var emptyitem = new TestableIItemStack(1, 1);
        emptyarray[0] = emptyitem;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[1];
        fillarray[0] = IItemStack.Empty;
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[1];
        var emptyitem = new TestableIItemStack(1, 5);
        emptyarray[0] = emptyitem;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[2];
        fillarray[0] = new TestableIItemStack(1, 63);
        fillarray[1] = new TestableIItemStack(1, 1);
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[2];
        var emptyitem0 = new TestableIItemStack(0);
        var emptyitem1 = new TestableIItemStack(1);
        emptyarray[0] = emptyitem0;
        emptyarray[1] = emptyitem1;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[2];
        fillarray[0] = IItemStack.Empty;
        fillarray[1] = IItemStack.Empty;
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[2];
        var emptyitem0 = new TestableIItemStack(1);
        var emptyitem1 = new TestableIItemStack(1);
        emptyarray[0] = emptyitem0;
        emptyarray[1] = emptyitem1;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[1];
        fillarray[0] = IItemStack.Empty;
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[1];
        var emptyitem = new TestableIItemStack(1, 1);
        emptyarray[0] = emptyitem;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[2];
        fillarray[0] = IItemStack.Empty;
        fillarray[1] = new TestableIItemStack(1, 1);
        var filling = new ItemStorageArray(fillarray);
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
        var emptyarray = new IItemStack[1];
        var emptyitem = new TestableIItemStack(1, 1);
        emptyarray[0] = emptyitem;
        var emptying = new ItemStorageArray(emptyarray);
        var fillarray = new IItemStack[1];
        fillarray[0] = new TestableIItemStack(2, 1);
        var filling = new ItemStorageArray(fillarray);
        // Act
        var ret = emptying.transferIntoStorage(filling, 1);
        // Assert
        MCAssert.assertIItemStackNotEmpty(emptying.get(0));
        Assertions.assertEquals(1, emptying.get(0).stackSize());
        MCAssert.assertIItemStackNotEmpty(filling.get(0));
        Assertions.assertEquals(1, filling.get(0).stackSize());
        Assertions.assertEquals(1, ret);
    }
}