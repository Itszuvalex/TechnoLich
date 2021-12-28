package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IModuleProvider {
    @NotNull @Nonnull
    <T> LazyOptional<T> getModule(@NotNull @Nonnull final IModule<T> module, final @Nullable Direction side);
}
