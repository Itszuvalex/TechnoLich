package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public interface IItemStack extends ICapabilitySerializable<CompoundTag>, IModuleProvider {
    public static IItemStack Empty = new IItemStack() {
        @Override public CompoundTag serializeNBT() { return new CompoundTag(); }
        @Override public void deserializeNBT(CompoundTag nbt) { }
        @NotNull @Override public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }
        @NotNull @Override public <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
            return LazyOptional.empty();
        }
        @Override public Item item() { return null; }
        @Override public int itemID() { return 0; }
        @Override public int stackSize() { return 0; }
        @Override public void setStackSize(int size) { }
        @Override public int stackSizeMax() { return 0; }
        @Override public int damage() { return 0; }
        @Override public void setDamage(int damage) { }
        @Override public int damageMax() { return 0; }
        @Override public @Nullable CompoundTag nbt() { return null; }
        @Override public boolean hasNbt() { return false; }
        @Override public @Nonnull ItemStack toMinecraft() { return ItemStack.EMPTY; }
        @Override public boolean isEmpty() { return true; }
        @Override public int room() { return 0; }
        @Override public @NotNull IItemStack copy() { return IItemStack.Empty; }
        @Override public boolean isItemEqual(@Nonnull IItemStack other) { return other.isEmpty(); }
        @Override public void writeToNBT(@Nonnull CompoundTag nbt) { }
    };

    Item item();
    int itemID();
    int stackSize();
    void setStackSize(int size);
    int stackSizeMax();
    int damage();
    void setDamage(int damage);
    int damageMax();
    @Nullable CompoundTag nbt();
    default boolean hasNbt() { return nbt() != null; }
    @Nonnull ItemStack toMinecraft();
    boolean isEmpty();
    default int room() { return stackSizeMax() - stackSize(); }
    @Nonnull IItemStack copy();
    boolean isItemEqual(@Nonnull IItemStack other);
    void writeToNBT(@Nonnull CompoundTag nbt);
}
