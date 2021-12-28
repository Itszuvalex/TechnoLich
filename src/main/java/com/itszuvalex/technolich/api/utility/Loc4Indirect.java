package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Loc4Indirect extends Loc4 {
    private ResourceKey<Level> worldId;

    public Loc4Indirect(ResourceKey<Level> worldId, BlockPos pos) {
        this.worldId = worldId;
        this.pos = pos;
    }

    @NotNull
    @Override
    public ResourceKey<Level> dimension() {
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
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
