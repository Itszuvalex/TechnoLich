package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;

public class MCAssert {
    public static void assertIItemStackEmpty(@NotNull @Nonnull IItemStack stack)
    {
        Assertions.assertSame(stack, IItemStack.Empty);
    }

    public static void assertIItemStackNotEmpty(@NotNull @Nonnull IItemStack stack)
    {
        Assertions.assertNotSame(stack, IItemStack.Empty);
    }
}
