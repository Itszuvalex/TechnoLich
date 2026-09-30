package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public interface IModuleProvider {
    @NotNull @Nonnull
    <T> Optional<T> getModule(@NotNull @Nonnull final IModule<T> module, final @Nullable Direction side);
}
