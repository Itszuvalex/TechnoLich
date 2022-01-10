package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TickableBlockEntityCore extends BlockEntityCore implements IBlockEntityTickable {
    public TickableBlockEntityCore(@NotNull BlockEntityType<?> type, @NotNull BlockPos pos, @NotNull BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState state) {
        fragList.tick(level, pos, state);
    }
}
