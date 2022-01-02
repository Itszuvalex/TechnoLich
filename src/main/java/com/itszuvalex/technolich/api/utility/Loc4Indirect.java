package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Loc4Indirect extends Loc4 {
    private final ResourceLocation worldId;

    public Loc4Indirect(ResourceLocation worldId, BlockPos pos) {
        this.worldId = worldId;
        this.pos = pos;
    }

    @Override
    public @NotNull ResourceLocation dimensionId() {
        return worldId;
    }

    @NotNull
    @Override
    public Loc4 getOffset(int x, int y, int z) {
        return Loc4.of(worldId, new BlockPos(x() + x, y() + y, z() + z));
    }

    @NotNull
    @Override
    public Loc4 copy() {
        return Loc4.of(worldId, new BlockPos(x(), y(), z()));
    }

    @Override
    public @NotNull Optional<IBlockEntity> getIBlockEntity(boolean force) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<BlockEntity> getBlockEntity(boolean force) {
        return Optional.empty();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
