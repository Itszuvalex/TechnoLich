package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class ItemStorageArray implements IItemStorage {
    private @NotNull
    @Nonnull
    final IItemStack[] storage;

    public ItemStorageArray(@NotNull @Nonnull IItemStack[] items) {
        this.storage = items;
        IntStream.range(0, storage.length)
                .filter((i) -> storage[i] == null)
                .forEach((i) -> storage[i] = IItemStack.Empty);
    }

    public ItemStorageArray(int size) {
        this(new IItemStack[size]);
    }

    @Override
    public @NotNull
    @Nonnull
    IItemStack get(int index) {
        return storage[index];
    }

    @Override
    public int size() {
        return storage.length;
    }

    @Override
    public void setSlot(int index, @NotNull @Nonnull IItemStack stack) {
        storage[index] = stack;
    }
}
