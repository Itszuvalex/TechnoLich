package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.utility.BoxCounter;
import com.itszuvalex.technolich.api.utility.MCConstants;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;


public interface IItemStorage extends ValueIOSerializable {
    IItemStorage Empty = new IItemStorage() {
        @Override
        public @NotNull IItemStack get(int index) {
            return IItemStack.Empty;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void setSlot(int index, @NotNull IItemStack stack) {
        }
    };

    @NotNull
    @Nonnull
    IItemStack get(int index);

    int size();

    /**
     * Replaces a slot.  Storages with a change listener notify it (see {@link #setChanged()}).
     */
    void setSlot(int index, @Nonnull @NotNull IItemStack stack);

    /**
     * Replaces a slot without notifying any change listener.  For callers that batch changes and call
     * {@link #setChanged()} themselves, e.g. inside a NeoForge transaction, where side effects must wait for commit.
     */
    default void setSlotQuietly(int index, @Nonnull @NotNull IItemStack stack) {
        setSlot(index, stack);
    }

    default boolean canInsert(int index, @NotNull @Nonnull IItemStack stack) {
        return true;
    }

    default int maxStackSize(int index) {
        return Math.min(MCConstants.ITEMSTACK_MAX, get(index).stackSizeMax());
    }

    default IItemStack split(int index, int amount) {
        var slot = get(index);
        var ret = slot.copy();
        if (amount >= slot.stackSize()) {
            setSlot(index, IItemStack.Empty);
        } else {
            slot.modifyStackSize(-amount);
            setSlot(index, slot); // Trigger updates
            ret.setStackSize(amount);
        }
        return ret;
    }

    /**
     * @param index Index to insert into
     * @param stack Stack to insert
     * @return IItemStack containing the leftovers from s.
     */
    default @NotNull
    @Nonnull
    IItemStack insert(int index, @NotNull @Nonnull IItemStack stack) {
        if (stack.isEmpty()) return stack;

        var max = Math.min(stack.stackSizeMax(), maxStackSize(index));
        var slot = get(index);
        if (slot.isEmpty()) {
            if (stack.stackSize() <= max) {
                // Copy so the caller's later mutations can't reach into this storage.
                setSlot(index, stack.copy());
                return IItemStack.Empty;
            }

            var sc = stack.copy();
            sc.setStackSize(max);
            var ret = stack.copy();
            ret.modifyStackSize(-max);
            setSlot(index, sc);
            return ret;
        }

        if (slot.isItemEqual(stack)) {
            var room = max - slot.stackSize();
            // The slot may already hold more than this insert allows (e.g. a smaller stack limit); insert nothing.
            if (room <= 0) return stack;
            if (stack.stackSize() <= room) {
                slot.modifyStackSize(stack.stackSize());
                setSlot(index, slot);
                return IItemStack.Empty;
            }

            var slotcopy = slot.copy();
            slotcopy.modifyStackSize(room);
            var ret = stack.copy();
            ret.modifyStackSize(-room);
            setSlot(index, slotcopy);
            return ret;
        }

        return stack;
    }

    default int transferSlotIntoStorageSlot(int slot, @NotNull @Nonnull IItemStorage storage, int targetSlot,
                                            int amount) {
        int transferRemaining = amount;
        var inSlot = get(slot).copy();
        var up = inSlot.copy();
        inSlot.setStackSize(Math.min(inSlot.stackSize(), transferRemaining));
        var ins = storage.insert(targetSlot, inSlot);
        int transfered = inSlot.stackSize() - ins.stackSize();
        transferRemaining -= transfered;
        up.modifyStackSize(-transfered);
        setSlot(slot, up.stackSize() <= 0 ? IItemStack.Empty : up);
        return transferRemaining;
    }

    default int transferSlotIntoStorage(int slot, @NotNull @Nonnull IItemStorage storage, int amount) {
        final var transferRemaining = new BoxCounter(amount);
        boolean completed =
                IntStream.range(0, storage.size()).filter((i) -> !storage.get(i).isEmpty()).filter((i) -> storage.canInsert(i, get(slot))).anyMatch((i) -> {
                    transferRemaining.set(transferSlotIntoStorageSlot(slot, storage, i, transferRemaining.get()));
                    return transferRemaining.get() <= 0;
                });

        if (completed) return 0;
        if (get(slot).isEmpty()) return transferRemaining.get();

        IntStream.range(0, storage.size()).filter((i) -> storage.get(i).isEmpty()).filter((i) -> storage.canInsert(i,
                get(slot))).anyMatch((i) -> {
            transferRemaining.set(transferSlotIntoStorageSlot(slot, storage, i, transferRemaining.get()));
            return transferRemaining.get() <= 0;
        });
        return transferRemaining.get();
    }

    default int transferIntoStorage(@NotNull @Nonnull IItemStorage storage, int amount) {
        if (storage == this) return amount;

        var transferRemaining = new BoxCounter(amount);
        IntStream.range(0, size()).filter((i) -> !get(i).isEmpty()).anyMatch((i) -> {
            transferRemaining.set(transferSlotIntoStorage(i, storage, transferRemaining.get()));
            return transferRemaining.get() <= 0;
        });
        return transferRemaining.get();
    }

    /**
     * Replaces every slot.  {@link #serialize} omits empty slots, so a missing slot is cleared rather than kept.
     */
    @Override
    default void deserialize(@NotNull @Nonnull ValueInput input) {
        IntStream.range(0, size()).forEach((i) ->
                setSlot(i, input.read(String.valueOf(i), IItemStack.codec()).orElse(IItemStack.Empty)));
    }

    @Override
    default void serialize(@NotNull @Nonnull ValueOutput output) {
        IntStream.range(0, size()).filter((i) -> !get(i).isEmpty()).forEach((i) ->
                output.store(String.valueOf(i), IItemStack.codec(), get(i)));
    }

    default boolean isEmpty() {
        return IntStream.range(0, size()).filter((i) -> !get(i).isEmpty()).findFirst().isEmpty();
    }

    /**
     * Notifies the storage's change listener, if any (e.g. the owning block entity's {@code setChanged}).
     */
    default void setChanged() {
    }
}
