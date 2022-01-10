package com.itszuvalex.technolich.api.adapters;

import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class Module<T> implements IModule<T> {
    private static HashMap<String, IModule<?>> MODULES = new HashMap<String, IModule<?>>();

    @Contract("_, _ -> param2")
    public static @Nonnull
    @NotNull <T> IModule<T> registerModule(@NotNull @Nonnull String name, @Nullable Supplier<Capability<T>> capabilitySupplier) {
        if (MODULES.containsKey(name))
            throw new IllegalArgumentException("Module with name: " + name + " already registered.");
        IModule<T> mod = new Module<>(name, capabilitySupplier);
        MODULES.put(name, mod);
        return mod;
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
    Supplier<Capability<T>> capabilitySupplier;

    @Override
    public @NotNull
    @Nonnull
    Optional<Capability<T>> capability() {
        return capabilitySupplier == null ? Optional.empty() : Optional.of(capabilitySupplier.get());
    }

    private Module(@NotNull @Nonnull String name, @Nullable Supplier<Capability<T>> capabilitySupplier) {
        this.name = name;
        this.capabilitySupplier = capabilitySupplier;
    }
}
