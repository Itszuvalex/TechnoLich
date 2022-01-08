package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface IBlockEntityTickable {
    void tick(@NotNull ILevel level, @NotNull BlockPos blockPos, @NotNull BlockState blockState);
}
