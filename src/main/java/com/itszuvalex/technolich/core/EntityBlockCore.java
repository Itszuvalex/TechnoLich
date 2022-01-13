package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class EntityBlockCore<T extends BlockEntity> extends Block implements EntityBlock {
    protected @NotNull
    @Nonnull
    final Supplier<BlockEntityType<T>> typeSupplier;

    public EntityBlockCore(Properties p_49795_, @NotNull @Nonnull Supplier<BlockEntityType<T>> typeSupplier) {
        super(p_49795_);
        this.typeSupplier = typeSupplier;
    }

    @Override
    public void onRemove(@NotNull BlockState blockStateNow, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockStatePrev, boolean p_60519_) {
        if (!blockStateNow.is(blockStatePrev.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(blockPos);
            if (blockentity instanceof IBlockEntityBlockEventHandler) {
                ((IBlockEntityBlockEventHandler) blockentity).onRemove(ILevel.of(level), blockPos, blockStatePrev);
            }

            super.onRemove(blockStateNow, level, blockPos, blockStatePrev, p_60519_);
        }
    }
}
