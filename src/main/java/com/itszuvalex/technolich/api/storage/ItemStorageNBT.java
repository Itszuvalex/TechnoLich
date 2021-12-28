package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ItemStorageNBT implements IItemStorage {
    private @NotNull @Nonnull final CompoundTag nbt;
    private final int size;
    public ItemStorageNBT(@NotNull @Nonnull CompoundTag nbt, int size) {
        this.nbt = nbt;
        this.size = size;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        return IItemStack.of(nbt.getCompound(String.valueOf(index)));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void setSlot(int index, @NotNull IItemStack stack) {
        nbt.put(String.valueOf(index), stack.serializeNBT());
    }
}
