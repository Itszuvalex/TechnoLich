package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.utility.SidedHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
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
        if(p_153214_ != typeSupplier.get())
            return null;
        if(!hasTicker(SidedHelper.sideFromIsClient(p_153212_.isClientSide())))
            return null;
        return TickableEntityBlockCore::tickInstance;
    }

    public boolean hasTicker(LogicalSide side) { return false; }

    private static <E extends BlockEntity> void tickInstance(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull E t) {
        ((TickableBlockEntityCore) t).tick(ILevel.of(level), blockPos, blockState);
    }
}
