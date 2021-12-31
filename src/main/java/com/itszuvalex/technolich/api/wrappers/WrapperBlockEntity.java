package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class WrapperBlockEntity implements IBlockEntity {
    private final @Nonnull BlockEntity entity;
    public WrapperBlockEntity(@NotNull @Nonnull BlockEntity entity) {
        this.entity = entity;
    }

    public @NotNull @Nonnull @Override
    BlockPos getBlockPos() { return entity.getBlockPos(); }

}
