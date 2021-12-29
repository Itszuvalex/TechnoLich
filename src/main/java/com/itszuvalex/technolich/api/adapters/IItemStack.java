package com.itszuvalex.technolich.api.adapters;

import com.itszuvalex.technolich.api.utility.INBTObjectSerializer;
import com.itszuvalex.technolich.api.utility.Overideable;
import com.itszuvalex.technolich.api.wrappers.WrapperVanillaItemStack;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public interface IItemStack extends ICapabilitySerializable<CompoundTag>, IModuleProvider {
    IItemStack Empty = new IItemStack() {
        @Override
        public CompoundTag serializeNBT() {return new CompoundTag();}

        @Override
        public void deserializeNBT(CompoundTag nbt) {}

        @NotNull
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull @Nonnull Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }

        @NotNull
        @Nonnull
        @Override
        public <T> LazyOptional<T> getModule(@NotNull @Nonnull IModule<T> module, @Nullable Direction side) {
            return LazyOptional.empty();
        }

        @Override
        public ResourceLocation item() {return ForgeRegistries.ITEMS.getDefaultKey();}

        @Override
        public int stackSize() {return 0;}

        @Override
        public void setStackSize(int size) {}

        @Override
        public int stackSizeMax() {return 64;}

        @Override
        public int damage() {return 0;}

        @Override
        public void setDamage(int damage) {}

        @Override
        public int damageMax() {return 0;}

        @Override
        public @Nullable CompoundTag nbt() {return null;}

        @Override
        public boolean hasNbt() {return false;}

        @Override
        public @NotNull
        @Nonnull
        ItemStack toMinecraft() {return ItemStack.EMPTY;}

        @Override
        public boolean isEmpty() {return true;}

        @Override
        public int room() {return 0;}

        @Override
        public @NotNull
        @Nonnull
        IItemStack copy() {return IItemStack.Empty;}

        @Override
        public boolean isItemEqual(@NotNull @Nonnull IItemStack other) {return other.isEmpty();}

        @Override
        public void writeToNBT(@NotNull @Nonnull CompoundTag nbt) {}
    };

    Overideable<INBTObjectSerializer<IItemStack, CompoundTag>> NBT_SERIALIZER = new Overideable<>(new INBTObjectSerializer<IItemStack, CompoundTag>() {
        @Override
        public void serialize(IItemStack obj, CompoundTag tag) {
            obj.writeToNBT(tag);
        }

        @Override
        public IItemStack deserialize(CompoundTag tag) {
            var stack = ItemStack.of(tag);
            return stack == ItemStack.EMPTY ? IItemStack.Empty : new WrapperVanillaItemStack(stack);
        }
    });

    static IItemStack of(CompoundTag nbt) {
        return NBT_SERIALIZER.get().deserialize(nbt);
    }

    ResourceLocation item();

    int stackSize();

    void setStackSize(int size);

    /**
     * THIS IS NOT VALIDATED.  USE ONLY WHEN YOU KNOW `change` IS SAFE.
     *
     * @param change Amount to modify (+1, -1).
     */
    default void modifyStackSize(int change) {
        setStackSize(stackSize() + change);
    }

    int stackSizeMax();

    int damage();

    void setDamage(int damage);

    int damageMax();

    @Nullable CompoundTag nbt();

    default boolean hasNbt() {
        var nbt = nbt();
        return nbt != null && !nbt.isEmpty();
    }

    @NotNull
    @Nonnull
    ItemStack toMinecraft();

    boolean isEmpty();

    default int room() {return stackSizeMax() - stackSize();}

    @NotNull
    @Nonnull
    IItemStack copy();

    boolean isItemEqual(@NotNull @Nonnull IItemStack other);

    void writeToNBT(@NotNull @Nonnull CompoundTag nbt);

}
