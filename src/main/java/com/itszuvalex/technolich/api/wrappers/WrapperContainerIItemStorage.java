package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.storage.IItemStorage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

//TODO Make sure this is correct
public class WrapperContainerIItemStorage implements Container {
    private @NotNull
    @Nonnull
    IItemStorage storage;

    public WrapperContainerIItemStorage(@NotNull IItemStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getContainerSize() {
        return storage.size();
    }

    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int p_18941_) {
        return storage.get(p_18941_).toMinecraft();
    }

    @Override
    public @NotNull ItemStack removeItem(int p_18942_, int p_18943_) {
        return storage.split(p_18942_, p_18943_).toMinecraft();
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int p_18951_) {
        var prev = storage.get(p_18951_);
        storage.setSlot(p_18951_, IItemStack.Empty);
        return prev.toMinecraft();
    }

    @Override
    public void setItem(int p_18944_, @NotNull ItemStack p_18945_) {
        storage.setSlot(p_18944_, IItemStack.of(p_18945_));
    }

    @Override
    public void setChanged() {
        storage.setChanged();
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return false;
    }

    @Override
    public void clearContent() {
    }
}
