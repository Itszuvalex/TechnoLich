package com.itszuvalex.technolich.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class TickableEntityBlockCore<T extends TickableBlockEntityCore> extends EntityBlockCore<T> implements EntityBlock {

    public TickableEntityBlockCore(Properties p_49795_, @NotNull Supplier<BlockEntityType<T>> typeSupplier) {
        super(p_49795_, typeSupplier);
    }

    @Nullable
    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(@NotNull Level p_153212_, @NotNull BlockState p_153213_, @NotNull BlockEntityType<E> p_153214_) {
        return p_153214_ == typeSupplier.get() ? this::tickInstance : null;
    }

    private <E extends BlockEntity> void tickInstance(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull E t) {
        ((T)t).tick(level, blockPos, blockState);
    }
}
