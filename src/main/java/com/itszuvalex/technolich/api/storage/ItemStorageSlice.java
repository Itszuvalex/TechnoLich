package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ItemStorageSlice implements IItemStorage {
    private @NotNull @Nonnull
    final IItemStorage storage;
    private @Nonnull
    final int[] slots;

    public ItemStorageSlice(@NotNull @Nonnull IItemStorage storage, @Nonnull int[] slots) {
        this.storage = storage;
        this.slots = slots;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        return storage.get(slots[index]);
    }

    @Override
    public int size() {
        return slots.length;
    }

    @Override
    public void setSlot(int index, @NotNull IItemStack stack) {
        storage.setSlot(slots[index], stack);
    }
}
