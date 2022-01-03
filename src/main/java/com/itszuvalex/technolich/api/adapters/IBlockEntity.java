package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IBlockEntity extends IModuleProvider {
    @NotNull
    @Nonnull
    BlockPos getBlockPos();
}
