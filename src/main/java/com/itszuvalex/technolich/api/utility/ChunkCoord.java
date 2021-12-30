package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public record ChunkCoord(int chunkX, int chunkZ) {
    public static ChunkCoord of(@NotNull @Nonnull BlockPos pos) {
        return new ChunkCoord(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static ChunkCoord of(@NotNull @Nonnull Loc4 loc) {
        return of(loc.pos);
    }

    public boolean inRangeOf(@NotNull @Nonnull ChunkCoord other, int blockRadius) {
        return (other.chunkX() >= (chunkX() - blockRadius) && other.chunkX() <= (chunkX() + blockRadius)) &&
                (other.chunkZ() >= (chunkZ() - blockRadius) && other.chunkZ() <= (chunkZ() + blockRadius));
    }
}
