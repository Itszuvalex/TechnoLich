package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

/**
 * Array-backed storage.  {@link #get} returns the live stack; mutate it only through this storage (e.g. {@link #setSlot})
 * so the change listener runs.
 */
public class ItemStorageArray implements IItemStorage {
    private @NotNull
    @Nonnull
    final IItemStack[] storage;
    private final @NotNull
    @Nonnull
    Runnable onChanged;

    /**
     * @param onChanged Run after every {@link #setSlot} and {@link #setChanged()}, e.g. the owning block entity's
     *                  {@code setChanged}.
     */
    public ItemStorageArray(@NotNull @Nonnull IItemStack[] items, @NotNull @Nonnull Runnable onChanged) {
        this.storage = items;
        this.onChanged = onChanged;
        IntStream.range(0, storage.length)
                .filter((i) -> storage[i] == null)
                .forEach((i) -> storage[i] = IItemStack.Empty);
    }

    public ItemStorageArray(@NotNull @Nonnull IItemStack[] items) {
        this(items, () -> {});
    }

    public ItemStorageArray(int size, @NotNull @Nonnull Runnable onChanged) {
        this(new IItemStack[size], onChanged);
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
        onChanged.run();
    }

    @Override
    public void setSlotQuietly(int index, @NotNull @Nonnull IItemStack stack) {
        storage[index] = stack;
    }

    @Override
    public void setChanged() {
        onChanged.run();
    }
}
