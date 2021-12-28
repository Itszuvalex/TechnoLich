package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class DynamicIItemStorage implements IItemStorage {
    private @NotNull @Nonnull final Supplier<IItemStorage> itemStorageSupplier;
    public DynamicIItemStorage(@NotNull @Nonnull Supplier<IItemStorage> itemStorageSupplier)
    {
        this.itemStorageSupplier = itemStorageSupplier;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        return itemStorageSupplier.get().get(index);
    }

    @Override
    public int size() {
        return itemStorageSupplier.get().size();
    }

    @Override
    public void setSlot(int index, @NotNull IItemStack stack) {
        itemStorageSupplier.get().setSlot(index, stack);
    }

    @Override
    public boolean canInsert(int index, @NotNull IItemStack stack) {
        return itemStorageSupplier.get().canInsert(index, stack);
    }

    @Override
    public int maxStackSize(int index) {
        return itemStorageSupplier.get().maxStackSize(index);
    }

    @Override
    public IItemStack split(int index, int amount) {
        return itemStorageSupplier.get().split(index, amount);
    }

    @Override
    public @NotNull IItemStack insert(int index, @NotNull IItemStack stack) {
        return itemStorageSupplier.get().insert(index, stack);
    }

    @Override
    public int transferSlotIntoStorageSlot(int slot, @NotNull IItemStorage storage, int targetSlot, int amount) {
        return itemStorageSupplier.get().transferSlotIntoStorageSlot(slot, storage, targetSlot, amount);
    }

    @Override
    public int transferSlotIntoStorage(int slot, @NotNull IItemStorage storage, int amount) {
        return itemStorageSupplier.get().transferSlotIntoStorage(slot, storage, amount);
    }

    @Override
    public int transferIntoStorage(@NotNull IItemStorage storage, int amount) {
        return itemStorageSupplier.get().transferIntoStorage(storage, amount);
    }

    @Override
    public void deserializeNBT(@NotNull CompoundTag nbt) {
        itemStorageSupplier.get().deserializeNBT(nbt);
    }

    @Override
    public @NotNull CompoundTag serializeNBT() {
        return itemStorageSupplier.get().serializeNBT();
    }

    @Override
    public void writeItemToNBT(@NotNull CompoundTag nbt, @NotNull IItemStack item, int slot) {
        itemStorageSupplier.get().writeItemToNBT(nbt, item, slot);
    }

    @Override
    public IItemStack readItemFromSlot(@NotNull CompoundTag nbt, int slot) {
        return itemStorageSupplier.get().readItemFromSlot(nbt, slot);
    }
}
