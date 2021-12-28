package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.IModuleProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class WrapperCapabilityProvider implements IModuleProvider {
    private final ICapabilityProvider capabilityProvider;

    public WrapperCapabilityProvider(@NotNull @Nonnull ICapabilityProvider capabilityProvider) {
        this.capabilityProvider = capabilityProvider;
    }

    @NotNull
    @Nonnull
    @Override
    public <T> LazyOptional<T> getModule(@NotNull @Nonnull IModule<T> module, @Nullable Direction side) {
        return module.capability().map(capabilityProvider::getCapability).orElseGet(LazyOptional::empty);
    }
}
