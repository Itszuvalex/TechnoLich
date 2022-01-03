package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class TestableLevel implements ILevel {
    private final @NotNull
    @Nonnull
    HashMap<BlockPos, IBlockEntity> blockEntityMap = new HashMap<>();

    private final @Nonnull @NotNull ResourceLocation dimension;

    public TestableLevel(ResourceLocation dim) {
        this.dimension = dim;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public ResourceKey<Level> dimension() {
        MCAssert.failVanillaClass("dimension");
        return null;
    }

    @Override
    public ResourceLocation dimensionLocation() {
        return dimension;
    }

    @Override
    public @NotNull Level toMinecraft() {
        MCAssert.failVanillaClass("toMinecraft");
        return null;
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return true;
    }

    @Override
    public IBlockEntity getIBlockEntity(BlockPos pos) {
        return blockEntityMap.get(pos);
    }

    @Override
    public void setIBlockEntity(IBlockEntity entity) {
        blockEntityMap.put(entity.getBlockPos(), entity);
    }

    @Override
    public void setBlockEntity(BlockEntity entity) {
        MCAssert.failVanillaClass("setBlockEntity");
    }
}
