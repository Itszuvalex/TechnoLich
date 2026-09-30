package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class WrapperVanillaItemStack implements IItemStack {
    private final ItemStack stack;

    public WrapperVanillaItemStack(@NotNull @Nonnull ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public Identifier item() {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
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
    public @NotNull DataComponentPatch components() {
        return stack.getComponentsPatch();
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
        return ItemStack.isSameItemSameComponents(stack, other.toMinecraft());
    }

    @NotNull
    @Nonnull
    @Override
    public <T> Optional<T> getModule(@NotNull @Nonnull IModule<T> module, @Nullable Direction side) {
        return module.itemCapability().map((cap) -> cap.getCapability(stack, ItemAccess.forStack(stack)));
    }
}
