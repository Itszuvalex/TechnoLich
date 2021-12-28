package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.wrappers.WrapperVanillaItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ItemStorageItemHandlerModifiable implements IItemStorage {
    private @NotNull
    @Nonnull
    final IItemHandlerModifiable handler;

    public ItemStorageItemHandlerModifiable(@NotNull @Nonnull IItemHandlerModifiable handler) {
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
        handler.setStackInSlot(index, stack.toMinecraft());
    }
}
