package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class Loc4 implements Comparable<Loc4>, INBTSerializable<CompoundTag> {
    protected @NotNull
    @Nonnull
    BlockPos pos;

    public abstract @NotNull
    @Nonnull
    ResourceKey<Level> dimension();

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

    @Override
    public int compareTo(@NotNull @Nonnull Loc4 o) {
        int dim = dimension().compareTo(o.dimension());
        if (dim != 0) return dim;

        int xl = Integer.compare(x(), o.x());
        if (xl != 0) return xl;

        int yl = Integer.compare(y(), o.y());
        if (yl != 0) return yl;

        return Integer.compare(z(), o.z());
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt(X_KEY, x());
        tag.putInt(Y_KEY, y());
        tag.putInt(Z_KEY, z());
        tag.putString(DIM_KEY, dimension().location().toString());
        return tag;
    }

    public static Loc4 of(@NotNull @Nonnull ResourceKey<Level> worldId, @NotNull @Nonnull BlockPos loc) {
        return new Loc4Indirect(worldId, loc);
    }

    public static Loc4 of(@NotNull @Nonnull Level level, @NotNull @Nonnull BlockPos loc) {
        return new Loc4Level(level, loc);
    }

    public static @NotNull Loc4 of(@NotNull @Nonnull CompoundTag nbt) {
        int x = nbt.getInt(X_KEY);
        int y = nbt.getInt(Y_KEY);
        int z = nbt.getInt(Z_KEY);
        String loc = nbt.getString(DIM_KEY);
        var key = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(loc));
        return Loc4.of(key, new BlockPos(x, y, z));
    }


    public static Loc4 ORIGIN = Loc4.of(Level.OVERWORLD, new BlockPos(0, 0, 0));
    public static String X_KEY = "x";
    public static String Y_KEY = "y";
    public static String Z_KEY = "z";
    public static String DIM_KEY = "dim";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loc4 loc4 = (Loc4) o;
        if (!dimension().location().equals(((Loc4) o).dimension().location())) return false;
        return pos.equals(loc4.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension().location(), pos);
    }
}

