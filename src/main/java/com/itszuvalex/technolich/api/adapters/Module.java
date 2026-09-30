package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Module<T> implements IModule<T> {
    /**
     * Concurrent because mods may register modules from parallel mod construction.
     */
    private static final Map<Identifier, IModule<?>> MODULES = new ConcurrentHashMap<>();

    @Contract("_, _ -> new")
    public static @Nonnull
    @NotNull <T> IModule<T> registerModule(@NotNull @Nonnull Identifier id, @Nullable BlockCapability<T, Direction> blockCapability) {
        return registerModule(id, blockCapability, null);
    }

    @Contract("_, _, _ -> new")
    public static @Nonnull
    @NotNull <T> IModule<T> registerModule(@NotNull @Nonnull Identifier id,
                                           @Nullable BlockCapability<T, Direction> blockCapability,
                                           @Nullable ItemCapability<T, ItemAccess> itemCapability) {
        IModule<T> mod = new Module<>(id, blockCapability, itemCapability);
        if (MODULES.putIfAbsent(id, mod) != null)
            throw new IllegalArgumentException("Module with id: " + id + " already registered.");
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
    Identifier id;

    @Override
    public @NotNull
    @Nonnull
    Identifier id() {
        return id;
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

    @Override
    public String toString() {
        return "Module[" + id + "]";
    }

    private Module(@NotNull @Nonnull Identifier id,
                   @Nullable BlockCapability<T, Direction> blockCapability,
                   @Nullable ItemCapability<T, ItemAccess> itemCapability) {
        this.id = id;
        this.blockCapability = blockCapability;
        this.itemCapability = itemCapability;
    }
}
