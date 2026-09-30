package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.TestableLevel;
import com.itszuvalex.technolich.TestableLoc4;
import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Records dirty/sync requests from fragments.
 */
public class TestableFragmentHost implements IFragmentHost {
    public final @NotNull
    @Nonnull
    IBlockEntity blockEntity =
            new TestableNetworkNodeBlockEntity(BlockPos.ZERO, new TestableLevel(TestableLoc4.DEFAULT_DIM));
    public int dirtyCount;
    public int syncCount;

    @Override
    public @NotNull IBlockEntity blockEntity() {
        return blockEntity;
    }

    @Override
    public void markDirty() {
        dirtyCount++;
    }

    @Override
    public void markDirtyAndSync() {
        dirtyCount++;
        syncCount++;
    }
}
