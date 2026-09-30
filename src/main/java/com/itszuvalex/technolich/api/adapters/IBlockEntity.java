package com.itszuvalex.technolich.api.adapters;

import com.itszuvalex.technolich.api.wrappers.WrapperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IBlockEntity extends IModuleProvider {
    /**
     * @return The block entity itself if it already implements IBlockEntity (e.g. a BlockEntityCore), so modules
     * without a block capability stay reachable; otherwise a capability-backed wrapper.
     */
    static @NotNull
    @Nonnull
    IBlockEntity of(@NotNull @Nonnull BlockEntity entity) {
        return entity instanceof IBlockEntity be ? be : new WrapperBlockEntity(entity);
    }

    @NotNull
    @Nonnull
    BlockPos getBlockPos();

    @NotNull
    @Nonnull
    BlockEntity toMinecraft();
}
