package com.itszuvalex.technolich.api.adapters;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface ILevel {
    boolean isClientSide();

    @NotNull
    @Nonnull
    Level toMinecraft();
}
