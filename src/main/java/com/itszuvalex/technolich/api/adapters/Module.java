package com.itszuvalex.technolich.api.adapters;

import it.unimi.dsi.fastutil.Hash;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.DrbgParameters;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class Module<T> implements IModule<T> {
    public static HashMap<String, IModule<?>> MODULES = new HashMap<String, IModule<?>>();
    public static <T> IModule<T> registerModule(@Nonnull String name, @Nullable Supplier<Capability<T>> capabilitySupplier) {
        if (MODULES.containsKey(name)) throw new IllegalArgumentException("Module with name: " + name + " already registered.");
        var mod = new Module(name, capabilitySupplier);
        MODULES.put(name, mod);
        return mod;
    }

    @TestOnly
    public static void clear() {
        MODULES.clear();
    }

    private final String name;

    public String name() { return name; }

    private final Supplier<Capability<T>> capabilitySupplier;

    @Override
    public @Nonnull
    Optional<Capability<T>> capability() {
        return capabilitySupplier == null ? Optional.empty() : Optional.of(capabilitySupplier.get());
    }

    private Module(@Nonnull String name, @Nullable Supplier<Capability<T>> capabilitySupplier) {
        this.name = name;
        this.capabilitySupplier = capabilitySupplier;
    }
}
