package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.storage.IItemStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Exposes an {@link IItemStorage} as a NeoForge item {@link ResourceHandler}, e.g. for the
 * {@link net.neoforged.neoforge.capabilities.Capabilities.Item#BLOCK} capability.
 * <p>
 * Honours {@link IItemStorage#canInsert} and the per-slot {@link IItemStorage#maxStackSize}.  Writes inside a
 * transaction go through {@link IItemStorage#setSlotQuietly}; {@link IItemStorage#setChanged()} runs once when the
 * root transaction commits.  Create one per storage and reuse it.
 */
public final class WrapperResourceHandlerIItemStorage implements ResourceHandler<ItemResource> {
    private final @NotNull
    @Nonnull
    IItemStorage storage;
    private final List<SlotWrapper> slots = new ArrayList<>();
    private final RootCommitJournal changedJournal;

    private WrapperResourceHandlerIItemStorage(@NotNull @Nonnull IItemStorage storage) {
        this.storage = storage;
        this.changedJournal = new RootCommitJournal(storage::setChanged);
    }

    public static @NotNull
    @Nonnull
    ResourceHandler<ItemResource> of(@NotNull @Nonnull IItemStorage storage) {
        return new WrapperResourceHandlerIItemStorage(storage);
    }

    private SlotWrapper slot(int index) {
        Objects.checkIndex(index, size());
        while (slots.size() <= index) slots.add(new SlotWrapper(slots.size()));
        return slots.get(index);
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public int insert(int index, @NotNull ItemResource resource, int amount, @NotNull TransactionContext transaction) {
        return slot(index).insert(0, resource, amount, transaction);
    }

    @Override
    public int extract(int index, @NotNull ItemResource resource, int amount, @NotNull TransactionContext transaction) {
        return slot(index).extract(0, resource, amount, transaction);
    }

    @Override
    public @NotNull ItemResource getResource(int index) {
        return slot(index).getResource(0);
    }

    @Override
    public long getAmountAsLong(int index) {
        return slot(index).getAmountAsLong(0);
    }

    @Override
    public long getCapacityAsLong(int index, @NotNull ItemResource resource) {
        return slot(index).getCapacityAsLong(0, resource);
    }

    @Override
    public boolean isValid(int index, @NotNull ItemResource resource) {
        return slot(index).isValid(0, resource);
    }

    private final class SlotWrapper extends ItemStackResourceHandler {
        private final int index;

        SlotWrapper(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack getStack() {
            return storage.get(index).toMinecraft();
        }

        @Override
        protected void setStack(ItemStack stack) {
            storage.setSlotQuietly(index, IItemStack.of(stack));
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return storage.canInsert(index, IItemStack.of(resource.toStack()));
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            int slotMax = storage.maxStackSize(index);
            return resource.isEmpty() ? slotMax : Math.min(slotMax, resource.getMaxStackSize());
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            super.updateSnapshots(transaction);
            changedJournal.updateSnapshots(transaction);
        }
    }
}
