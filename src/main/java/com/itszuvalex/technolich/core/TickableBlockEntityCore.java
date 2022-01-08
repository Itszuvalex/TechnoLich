package com.itszuvalex.technolich.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TickableBlockEntityCore extends BlockEntityCore {
    public TickableBlockEntityCore(@NotNull BlockEntityType<?> type, @NotNull BlockPos pos, @NotNull BlockState state) {
        super(type, pos, state);
    }

    public void tick(Level p_155253_, BlockPos p_155254_, BlockState p_155255_) {
    }
}
