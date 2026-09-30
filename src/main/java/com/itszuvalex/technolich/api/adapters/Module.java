package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class Module<T> implements IModule<T> {
    private static HashMap<String, IModule<?>> MODULES = new HashMap<String, IModule<?>>();

    @Contract("_, _ -> new")
    public static @Nonnull
    @NotNull <T> IModule<T> registerModule(@NotNull @Nonnull String name, @Nullable BlockCapability<T, Direction> blockCapability) {
        return registerModule(name, blockCapability, null);
    }

    @Contract("_, _, _ -> new")
    public static @Nonnull
    @NotNull <T> IModule<T> registerModule(@NotNull @Nonnull String name,
                                           @Nullable BlockCapability<T, Direction> blockCapability,
                                           @Nullable ItemCapability<T, ItemAccess> itemCapability) {
        if (MODULES.containsKey(name))
            throw new IllegalArgumentException("Module with name: " + name + " already registered.");
        IModule<T> mod = new Module<>(name, blockCapability, itemCapability);
        MODULES.put(name, mod);
        return mod;
    }

    public static @NotNull
    @Nonnull
    Collection<IModule<?>> modules() {
        return Collections.unmodifiableCollection(MODULES.values());
    }

    @TestOnly
    public static void clear() {
        MODULES.clear();
    }

    private final @NotNull
    @Nonnull
    String name;

    public @NotNull
    @Nonnull
    String name() {
        return name;
    }

    private final @Nullable
    BlockCapability<T, Direction> blockCapability;

    private final @Nullable
    ItemCapability<T, ItemAccess> itemCapability;

    @Override
    public @NotNull
    @Nonnull
    Optional<BlockCapability<T, Direction>> blockCapability() {
        return Optional.ofNullable(blockCapability);
    }

    @Override
    public @NotNull
    @Nonnull
    Optional<ItemCapability<T, ItemAccess>> itemCapability() {
        return Optional.ofNullable(itemCapability);
    }

    private Module(@NotNull @Nonnull String name,
                   @Nullable BlockCapability<T, Direction> blockCapability,
                   @Nullable ItemCapability<T, ItemAccess> itemCapability) {
        this.name = name;
        this.blockCapability = blockCapability;
        this.itemCapability = itemCapability;
    }
}
