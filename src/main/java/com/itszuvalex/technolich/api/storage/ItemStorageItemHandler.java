package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.wrappers.WrapperVanillaItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ItemStorageItemHandler implements IItemStorage {
    private @NotNull @Nonnull
    final IItemHandler handler;
    public ItemStorageItemHandler(@NotNull @Nonnull IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        return new WrapperVanillaItemStack(handler.getStackInSlot(index));
    }

    @Override
    public int size() {
        return handler.getSlots();
    }

    @Override
    public void setSlot(int index, @NotNull @Nonnull IItemStack stack) {
        handler.extractItem(index, handler.getStackInSlot(index).getCount(), false);
        handler.insertItem(index, stack.toMinecraft(), false);
    }
}
