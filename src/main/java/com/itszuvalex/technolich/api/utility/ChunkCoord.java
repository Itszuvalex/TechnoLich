package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;

public record ChunkCoord(int chunkX, int chunkZ) {
    public static ChunkCoord of(BlockPos pos) {
        return new ChunkCoord(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static ChunkCoord of(Loc4 loc) {
        return of(loc.pos);
    }
}
