package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public record ChunkCoord(int chunkX, int chunkZ) {
    public static @Nonnull ChunkCoord of(@NotNull @Nonnull BlockPos pos) {
        return new ChunkCoord(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static @Nonnull ChunkCoord of(@NotNull @Nonnull Loc4 loc) {
        return of(loc.pos);
    }

    public boolean inRangeOf(@NotNull @Nonnull ChunkCoord other, int chunkRadius) {
        return (other.chunkX() >= (chunkX() - chunkRadius) && other.chunkX() <= (chunkX() + chunkRadius)) &&
                (other.chunkZ() >= (chunkZ() - chunkRadius) && other.chunkZ() <= (chunkZ() + chunkRadius));
    }
}
