package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Item storage backed by a live CompoundTag, one entry per slot.
 */
public class ItemStorageNBT implements IItemStorage {
    private @NotNull @Nonnull final CompoundTag nbt;
    private final int size;
    private @NotNull @Nonnull final DynamicOps<Tag> ops;
    private @NotNull @Nonnull final Runnable onChanged;

    /**
     * Without registry context, items with datapack-registry components (e.g. enchantments) will fail to encode.
     */
    public ItemStorageNBT(@NotNull @Nonnull CompoundTag nbt, int size) {
        this(nbt, size, NbtOps.INSTANCE, () -> {});
    }

    public ItemStorageNBT(@NotNull @Nonnull CompoundTag nbt, int size, @NotNull @Nonnull HolderLookup.Provider registries) {
        this(nbt, size, registries, () -> {});
    }

    /**
     * @param onChanged Run after every {@link #setSlot} and {@link #setChanged()}, e.g. to write the tag back into
     *                  an item's data component.
     */
    public ItemStorageNBT(@NotNull @Nonnull CompoundTag nbt, int size, @NotNull @Nonnull HolderLookup.Provider registries,
                          @NotNull @Nonnull Runnable onChanged) {
        this(nbt, size, registries.createSerializationContext(NbtOps.INSTANCE), onChanged);
    }

    private ItemStorageNBT(@NotNull @Nonnull CompoundTag nbt, int size, @NotNull @Nonnull DynamicOps<Tag> ops,
                           @NotNull @Nonnull Runnable onChanged) {
        this.nbt = nbt;
        this.size = size;
        this.ops = ops;
        this.onChanged = onChanged;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        var tag = nbt.get(String.valueOf(index));
        if (tag == null) return IItemStack.Empty;
        return IItemStack.codec().parse(ops, tag).result().orElse(IItemStack.Empty);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void setSlot(int index, @NotNull IItemStack stack) {
        setSlotQuietly(index, stack);
        onChanged.run();
    }

    @Override
    public void setSlotQuietly(int index, @NotNull IItemStack stack) {
        if (stack.isEmpty()) {
            nbt.remove(String.valueOf(index));
            return;
        }
        nbt.put(String.valueOf(index), IItemStack.codec().encodeStart(ops, stack).getOrThrow());
    }

    @Override
    public void setChanged() {
        onChanged.run();
    }
}
