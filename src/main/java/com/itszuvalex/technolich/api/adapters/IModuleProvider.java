package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IModuleProvider {
    @Nonnull
    <T> LazyOptional<T> getModule(@Nonnull final IModule<T> module, final @Nullable Direction side);
}
