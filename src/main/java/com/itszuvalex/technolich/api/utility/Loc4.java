package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public abstract class Loc4 implements Comparable<Loc4> {
    protected @NotNull
    @Nonnull
    BlockPos pos;

    public abstract @NotNull
    @Nonnull
    Identifier dimensionId();

    public abstract @NotNull
    @Nonnull
    Loc4 getOffset(int x, int y, int z);

    public abstract @NotNull
    @Nonnull
    Loc4 copy();

    public int x() {
        return pos.getX();
    }

    public int y() {
        return pos.getY();
    }

    public int z() {
        return pos.getZ();
    }

    public @NotNull
    @Nonnull
    Loc4 getOffset(@NotNull @Nonnull BlockPos offset) {
        return getOffset(offset.getX(), offset.getY(), offset.getZ());
    }

    public @NotNull
    @Nonnull
    BlockPos getPos() {
        return pos;
    }

    public @NotNull
    @Nonnull
    ChunkCoord getChunkCoords() {
        return ChunkCoord.of(getPos());
    }

    public @NotNull
    @Nonnull
    Loc4 anchor(@NotNull @Nonnull Level level) {
        return new Loc4Level(level, pos);
    }

    public @NotNull
    @Nonnull
    Loc4 anchor(@NotNull @Nonnull ILevel level) {
        return new Loc4ILevel(level, pos);
    }

    public abstract @NotNull
    @Nonnull
    Optional<IBlockEntity> getIBlockEntity(boolean force);

    public abstract @NotNull
    @Nonnull
    Optional<BlockEntity> getBlockEntity(boolean force);

    public double distSqr(@NotNull @Nonnull Loc4 other) {
        if (!other.dimensionId().equals(dimensionId())) return Double.MAX_VALUE;
        return distSqr(other.x(), other.y(), other.z());
    }

    public double distSqr(int x, int y, int z) {
        return (x() - x) * (x() - x) + (y() - y) * (y() - y) + (z() - z) * (z() - z);
    }

    public double dist(@NotNull @Nonnull Loc4 other) {
        if (!other.dimensionId().equals(dimensionId())) return Float.MAX_VALUE;
        return dist(other.x(), other.y(), other.z());
    }

    public double dist(int x, int y, int z) {
        return Math.sqrt(distSqr(x, y, z));
    }

    @Override
    public int compareTo(@NotNull @Nonnull Loc4 o) {
        int dim = dimensionId().compareTo(o.dimensionId());
        if (dim != 0) return dim;

        int xl = Integer.compare(x(), o.x());
        if (xl != 0) return xl;

        int yl = Integer.compare(y(), o.y());
        if (yl != 0) return yl;

        return Integer.compare(z(), o.z());
    }

    public static Loc4 of(@NotNull @Nonnull Identifier worldId, @NotNull @Nonnull BlockPos loc) {
        return new Loc4Indirect(worldId, loc);
    }

    public static Loc4 of(@NotNull @Nonnull Level level, @NotNull @Nonnull BlockPos loc) {
        return new Loc4Level(level, loc);
    }

    public static Loc4 of(@NotNull @Nonnull ILevel level, @NotNull @Nonnull BlockPos loc) {
        return new Loc4ILevel(level, loc);
    }

    public static Loc4 ORIGIN = Loc4.of(Identifier.parse("overworld"), new BlockPos(0, 0, 0));
    public static String X_KEY = "x";
    public static String Y_KEY = "y";
    public static String Z_KEY = "z";
    public static String DIM_KEY = "dim";

    /**
     * Serializes any Loc4 by value; decodes to a {@link Loc4Indirect}.
     */
    public static final Codec<Loc4> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.INT.fieldOf(X_KEY).forGetter(Loc4::x),
            Codec.INT.fieldOf(Y_KEY).forGetter(Loc4::y),
            Codec.INT.fieldOf(Z_KEY).forGetter(Loc4::z),
            Identifier.CODEC.fieldOf(DIM_KEY).forGetter(Loc4::dimensionId)
    ).apply(instance, (x, y, z, dim) -> Loc4.of(dim, new BlockPos(x, y, z))));

    /**
     * Value equality on dimension and position, regardless of subclass, so e.g. a {@link Loc4Level} and a decoded
     * {@link Loc4Indirect} for the same block are interchangeable as map keys.  Consistent with {@link #compareTo}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loc4 loc4)) return false;
        if (!dimensionId().equals(loc4.dimensionId())) return false;
        return pos.equals(loc4.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensionId(), pos);
    }

    public boolean isNeighbor(Loc4 loc) {
        if(!dimensionId().equals(loc.dimensionId())) return false;
        if(Math.abs(x() - loc.x()) == 1 && y() == loc.y() && z() == loc.z()) return true;
        if(x() == loc.x() && Math.abs(y() - loc.y()) == 1 && z() == loc.z()) return true;
        return x() == loc.x() && y() == loc.y() && Math.abs(z() - loc.z()) == 1;
    }
}

