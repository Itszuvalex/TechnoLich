package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Item storage backed by a NeoForge item {@link ResourceHandler}, e.g. another mod's inventory capability.
 * Mutations open root transactions, so they must not be called while a transaction is open.
 */
public class ItemStorageResourceHandler implements IItemStorage {
    private @NotNull @Nonnull
    final ResourceHandler<ItemResource> handler;

    public ItemStorageResourceHandler(@NotNull @Nonnull ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        var resource = handler.getResource(index);
        if (resource.isEmpty()) return IItemStack.Empty;
        return IItemStack.of(resource.toStack(handler.getAmountAsInt(index)));
    }

    @Override
    public int size() {
        return handler.size();
    }

    /**
     * Replaces the slot's contents.  Leaves the slot unchanged if the handler will not accept the full stack.
     */
    @Override
    public void setSlot(int index, @NotNull @Nonnull IItemStack stack) {
        try (var tx = Transaction.openRoot()) {
            var current = handler.getResource(index);
            var currentAmount = handler.getAmountAsInt(index);
            if (!current.isEmpty() && handler.extract(index, current, currentAmount, tx) != currentAmount) return;
            if (!stack.isEmpty()) {
                var resource = ItemResource.of(stack.toMinecraft());
                if (handler.insert(index, resource, stack.stackSize(), tx) != stack.stackSize()) return;
            }
            tx.commit();
        }
    }

    @Override
    public boolean canInsert(int index, @NotNull @Nonnull IItemStack stack) {
        return stack.isEmpty() || handler.isValid(index, ItemResource.of(stack.toMinecraft()));
    }
}
