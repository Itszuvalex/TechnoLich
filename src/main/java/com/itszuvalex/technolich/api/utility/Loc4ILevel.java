package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.wrappers.WrapperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Loc4ILevel extends Loc4 {
    private ILevel level;

    public Loc4ILevel(@NotNull @Nonnull ILevel level, @NotNull @Nonnull BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @NotNull
    @Nonnull
    public Level level() {
        return level.toMinecraft();
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
    public @NotNull Optional<IBlockEntity> getIBlockEntity(boolean force) {
        if (force || level.isLoaded(getPos()))
            return Optional.ofNullable(level.getIBlockEntity(getPos()));
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<BlockEntity> getBlockEntity(boolean force) {
        if (force || level.isLoaded(getPos()))
            return Optional.ofNullable(level.getIBlockEntity(getPos())).map(IBlockEntity::toMinecraft);
        return Optional.empty();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
