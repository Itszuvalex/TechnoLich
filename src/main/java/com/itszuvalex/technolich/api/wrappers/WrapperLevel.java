package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class WrapperLevel implements ILevel {
    private static final int CACHE_SIZE = 16;
    private static final WrapperCache<Level, WrapperLevel> cache = new WrapperCache<>(CACHE_SIZE, WrapperLevel::new);

    public static @NotNull WrapperLevel of(@NotNull Level level) {
        return cache.get(level);
    }

    private @Nonnull
    final Level level;

    public WrapperLevel(@NotNull @Nonnull Level level) {
        this.level = level;
    }

    @Override
    public boolean isClientSide() {
        return level.isClientSide();
    }

    @Override
    public ResourceKey<Level> dimension() {
        return level.dimension();
    }

    @Override
    public ResourceLocation dimensionLocation() {
        return dimension().location();
    }

    @Override
    public @NotNull Level toMinecraft() {
        return level;
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return level.isLoaded(pos);
    }

    @Override
    public IBlockEntity getIBlockEntity(BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;
        return new WrapperBlockEntity(blockEntity);
    }

    @Override
    public void setIBlockEntity(IBlockEntity entity) {
        level.setBlockEntity(entity.toMinecraft());
    }

    @Override
    public void setBlockEntity(BlockEntity entity) {
        level.setBlockEntity(entity);
    }
}
