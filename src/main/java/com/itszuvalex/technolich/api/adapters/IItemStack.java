package com.itszuvalex.technolich.api.adapters;

import com.itszuvalex.technolich.api.utility.MCConstants;
import com.itszuvalex.technolich.api.utility.Overideable;
import com.itszuvalex.technolich.api.wrappers.WrapperVanillaItemStack;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface IItemStack extends IModuleProvider {
    IItemStack Empty = new IItemStack() {
        @NotNull
        @Nonnull
        @Override
        public <T> Optional<T> getModule(@NotNull @Nonnull IModule<T> module, @Nullable Direction side) {
            return Optional.empty();
        }

        @Override
        public Identifier item() {
            return BuiltInRegistries.ITEM.getDefaultKey();
        }

        @Override
        public int stackSize() {
            return 0;
        }

        @Override
        public void setStackSize(int size) {
        }

        @Override
        public int stackSizeMax() {
            return MCConstants.ITEMSTACK_MAX;
        }

        @Override
        public int damage() {
            return 0;
        }

        @Override
        public void setDamage(int damage) {
        }

        @Override
        public int damageMax() {
            return 0;
        }

        @Override
        public @NotNull DataComponentPatch components() {
            return DataComponentPatch.EMPTY;
        }

        @Override
        public boolean hasComponents() {
            return false;
        }

        @Override
        public @NotNull
        @Nonnull
        ItemStack toMinecraft() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int room() {
            return 0;
        }

        @Override
        public @NotNull
        @Nonnull
        IItemStack copy() {
            return IItemStack.Empty;
        }

        @Override
        public boolean isItemEqual(@NotNull @Nonnull IItemStack other) {
            return other.isEmpty();
        }
    };

    /**
     * Codec used to persist IItemStacks.  Overridable so tests can serialize without vanilla ItemStacks.
     */
    Overideable<Codec<IItemStack>> CODEC = new Overideable<>(
            ItemStack.OPTIONAL_CODEC.xmap(IItemStack::of, IItemStack::toMinecraft));

    static @NotNull Codec<IItemStack> codec() {
        return CODEC.get();
    }

    static @NotNull IItemStack of(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return IItemStack.Empty;
        return new WrapperVanillaItemStack(stack);
    }

    Identifier item();

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

    /**
     * @return Data components that differ from the item's defaults.
     */
    @NotNull
    @Nonnull
    DataComponentPatch components();

    default boolean hasComponents() {
        return !components().isEmpty();
    }

    @NotNull
    @Nonnull
    ItemStack toMinecraft();

    boolean isEmpty();

    default int room() {
        return stackSizeMax() - stackSize();
    }

    @NotNull
    @Nonnull
    IItemStack copy();

    boolean isItemEqual(@NotNull @Nonnull IItemStack other);

}
