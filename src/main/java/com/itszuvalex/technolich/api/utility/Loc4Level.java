package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class Loc4Level extends Loc4 {
    private Level level;

    public Loc4Level(@NotNull @Nonnull Level level, @NotNull @Nonnull BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @NotNull
    @Nonnull
    public Level level() {
        return level;
    }

    @Override
    public @NotNull ResourceLocation dimensionId() {
        return level().dimension().location();
    }

    @NotNull
    @Override
    public Loc4 getOffset(int x, int y, int z) {
        return Loc4.of(level, new BlockPos(x() + x, y() + y, z() + z));
    }

    @NotNull
    @Override
    public Loc4 copy() {
        return Loc4.of(level, new BlockPos(x(), y(), z()));
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
