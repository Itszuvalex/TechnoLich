package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.utility.BoxCounter;
import com.itszuvalex.technolich.api.utility.MCConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;


public interface IItemStorage extends INBTSerializable<CompoundTag> {
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

    void setSlot(int index, @Nonnull @NotNull IItemStack stack);

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
                setSlot(index, stack);
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

    @Override
    default void deserializeNBT(@NotNull @Nonnull CompoundTag nbt) {
        IntStream.range(0, size()).filter((i) -> nbt.contains(String.valueOf(i))).forEach((i) -> setSlot(i,
                readItemFromSlot(nbt, i)));
    }

    @Override
    default @NotNull
    @Nonnull
    CompoundTag serializeNBT() {
        var ret = new CompoundTag();
        IntStream.range(0, size()).filter((i) -> !get(i).isEmpty()).forEach((i) ->
                writeItemToNBT(ret, get(i), i));
        return ret;
    }

    default void writeItemToNBT(@NotNull @Nonnull CompoundTag nbt, @NotNull @Nonnull IItemStack item, int slot) {
        nbt.put(String.valueOf(slot), item.serializeNBT());
    }

    default IItemStack readItemFromSlot(@NotNull @Nonnull CompoundTag nbt, int slot) {
        return IItemStack.of(nbt.getCompound(String.valueOf(slot)));
    }

    default boolean isEmpty() {
        return IntStream.range(0, size()).filter((i) -> !get(i).isEmpty()).findFirst().isEmpty();
    }

    default void setChanged() {
    }
}
