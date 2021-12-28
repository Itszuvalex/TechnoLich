package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class WrapperVanillaItemStack implements IItemStack {
    private ItemStack stack;

    public WrapperVanillaItemStack(@NotNull @Nonnull ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ResourceLocation item() {
        return stack.getItem().getRegistryName();
    }

    @Override
    public int stackSize() {
        return stack.getCount();
    }

    @Override
    public void setStackSize(int size) {
        stack.setCount(size);
    }

    @Override
    public int stackSizeMax() {
        return stack.getMaxStackSize();
    }

    @Override
    public int damage() {
        return stack.getDamageValue();
    }

    @Override
    public void setDamage(int damage) {
        stack.setDamageValue(damage);
    }

    @Override
    public int damageMax() {
        return stack.getMaxDamage();
    }

    @Override
    public @Nullable CompoundTag nbt() {
        return stack.getShareTag();
    }

    @NotNull
    @Nonnull
    @Override
    public ItemStack toMinecraft() {
        return stack;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @NotNull
    @Nonnull
    @Override
    public IItemStack copy() {
        return new WrapperVanillaItemStack(stack.copy());
    }

    @Override
    public boolean isItemEqual(@NotNull @Nonnull IItemStack other) {
        return stack.equals(other.toMinecraft(), false);
    }

    @Override
    public void writeToNBT(@NotNull @Nonnull CompoundTag nbt) {
        stack.save(nbt);
    }

    @NotNull
    @Nonnull
    @Override
    public <T> LazyOptional<T> getModule(@NotNull @Nonnull IModule<T> module, @Nullable Direction side) {
        return module.capability().map(this::getCapability).orElseGet(LazyOptional::empty);
    }

    @NotNull
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull @Nonnull Capability<T> cap, @Nullable Direction side) {
        return stack.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        return stack.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        stack = ItemStack.of(nbt);
    }
}
