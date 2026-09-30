package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * What a fragment can ask of the block entity that owns it.  Handed to each fragment through
 * {@link IInternalBlockEntityFragment#onAttach} when it is added to a {@link BlockEntityFragmentCollection}.
 */
public interface IFragmentHost {
    @NotNull
    @Nonnull
    IBlockEntity blockEntity();

    /**
     * Marks the block entity as changed so it is saved.  Call after changing LEVEL-scope state.
     */
    void markDirty();

    /**
     * {@link #markDirty()}, and on the server also re-sends DESCRIPTION-scope data to clients tracking the chunk.
     * Call after changing state that clients render or display.
     */
    void markDirtyAndSync();
}
