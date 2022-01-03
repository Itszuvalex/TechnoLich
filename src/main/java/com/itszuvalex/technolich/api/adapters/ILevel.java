package com.itszuvalex.technolich.api.adapters;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface ILevel {
    boolean isClientSide();

    ResourceKey<Level> dimension();

    ResourceLocation dimensionLocation();

    @NotNull
    @Nonnull
    Level toMinecraft();

    boolean isLoaded(BlockPos pos);

    IBlockEntity getIBlockEntity(BlockPos pos);

    void setIBlockEntity(IBlockEntity entity);

    void setBlockEntity(BlockEntity entity);
}
