package com.itszuvalex.technolich.api.storage;

import com.ibm.icu.impl.Assert;
import com.itszuvalex.technolich.MCAssert;
import com.itszuvalex.technolich.TestableIItemStack;
import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

class ItemStorageArrayTest {
    static class TestState {
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

        public TestState() {
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
    }

    public static TestState getState() {
        return new TestState();
    }

    @Test
    void Size_EqualsArrayLength() {
        var state = getState();
        Assertions.assertEquals(state.array.length, state.storage.size());
    }

    @Test
    void Get_ReturnUnderlyingElementsOfStorage() {
        var state = getState();
        IntStream.range(0, state.array.length).forEach((i) ->
                Assertions.assertSame(state.array[i], state.storage.get(i))
        );
    }

    @Test
    void SetSlot_UpdateUnderlyingStorage() {
        int index = 7;
        var state = getState();
        state.storage.setSlot(index, new TestableIItemStack(5));
        Assertions.assertSame(state.array[index], state.storage.get(index));
    }

    @Test
    void Split_OnEmptyReturnEmpty() {
        var state = getState();
        var ret = state.storage.split(7, 1);
        MCAssert.assertIItemStackEmpty(ret);
    }

    @Test
    void Split_OnLessThanStackModifiesAndReturns() {
        var state = getState();
        var ret = state.storage.split(1, 2);
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertEquals(ret.stackSize(), 2);
        var contents = state.storage.get(1);
        MCAssert.assertIItemStackNotEmpty(contents);
        Assertions.assertEquals(contents.stackSize(), 3);
    }

    @Test
    void Split_OnExactStackModifiesEmptiesAndReturns() {
        var state = getState();
        var ret = state.storage.split(1, 5);
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertEquals(ret.stackSize(), 5);
        MCAssert.assertIItemStackEmpty(state.storage.get(1));
    }

    @Test
    void Split_OnMoreModifiesEmptiesAndReturnsContents() {
        var state = getState();
        var ret = state.storage.split(1, 10);
        MCAssert.assertIItemStackNotEmpty(ret);
        Assertions.assertEquals(ret.stackSize(), 5);
        MCAssert.assertIItemStackEmpty(state.storage.get(1));
    }

    @Test
    void Insert_EmptyItemStackModifyNothingReturnEmpty() {
        var state = getState();
        var ins  = IItemStack.Empty;
        var copy = state.storage.get(1).copy();
        Assertions.assertNotSame(copy, state.storage.get(1));
        MCAssert.assertIItemStackEmpty(state.storage.insert(1, ins));
        Assertions.assertTrue(copy.isItemEqual(state.storage.get(1)));
    }

    @Test
    void Insert_ItemIntoEmptyInsertWholeItemReturnEmpty() {
        var state = getState();
        var ins = new TestableIItemStack(1, 2);
        MCAssert.assertIItemStackEmpty(state.storage.get(9));
        MCAssert.assertIItemStackEmpty(state.storage.insert(9, ins));
        Assertions.assertSame(state.storage.get(9), ins);
    }
}