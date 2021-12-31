package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.Loc4;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Uses Curious-Recurring-Template-Pattern
 * @param <C> The derived class of the Nodes comprising this network.
 * @param <N> The derived class of the Network
 */
public interface INetworkNode<C extends INetworkNode<C, N>, N extends INetwork<C, N>> {
    void setNetwork(@Nonnull @NotNull N network);

    @NotNull
    @Nonnull
    N getNetwork();

    @NotNull
    @Nonnull
    Loc4 getLoc();

    void refresh();

    boolean canConnect(@NotNull @Nonnull Loc4 loc);

    boolean canAdd(@NotNull @Nonnull N network);

    // Events
    void onAdded(@NotNull @Nonnull N network);

    void onRemoved(@NotNull @Nonnull N network);

    void onConnect(@NotNull @Nonnull Loc4 loc);

    void onDisconnect(@NotNull @Nonnull Loc4 loc);
}
