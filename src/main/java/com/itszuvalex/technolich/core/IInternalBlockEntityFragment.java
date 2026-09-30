package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.IScopedSerialization;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IInternalBlockEntityFragment extends IBlockEntityEventHandler, IBlockEntityBlockEventHandler, IScopedSerialization {
    @NotNull
    @Nonnull
    String name();

    /**
     * Called once when the fragment is added to its block entity's fragment collection.
     */
    default void onAttach(@NotNull @Nonnull IFragmentHost host) {
    }
}
