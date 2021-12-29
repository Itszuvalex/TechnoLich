package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;

public class MCAssert {
    public static void assertIItemStackEmpty(@NotNull @Nonnull IItemStack stack) {
        boolean isEmptyStack = stack == IItemStack.Empty;
        if (isEmptyStack)
            Assertions.assertSame(stack, IItemStack.Empty);
        else
            Assertions.assertTrue(stack.isEmpty());
    }

    public static void assertIItemStackNotEmpty(@NotNull @Nonnull IItemStack stack) {
        Assertions.assertNotSame(stack, IItemStack.Empty);
        Assertions.assertFalse(stack.isEmpty());
    }
}
