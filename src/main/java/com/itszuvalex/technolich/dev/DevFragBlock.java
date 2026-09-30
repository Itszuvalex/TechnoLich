package com.itszuvalex.technolich.dev;

import com.itszuvalex.technolich.core.EntityBlockCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DevFragBlock extends EntityBlockCore<DevFragBlockEntity> {
    public DevFragBlock(Properties properties) {
        super(properties, DevContent.DEV_FRAG_BLOCK_ENTITY);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DevFragBlockEntity(pos, state);
    }
}
