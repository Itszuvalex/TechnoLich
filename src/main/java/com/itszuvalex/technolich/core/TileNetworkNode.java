package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.Loc4;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class TileNetworkNode<C extends TileNetworkNode<C, N>, N extends TileNetwork<C, N>> implements
        INetworkNode<C, N> {
    protected @NotNull
    @Nonnull
    N network;

    @Override
    public void setNetwork(@NotNull N network) {this.network = network;}

    @Override
    public @NotNull N getNetwork() {return network;}

    @Override
    public boolean canConnect(@NotNull Loc4 loc) {return true;}

    @Override
    public boolean canAdd(@NotNull N network) {return true;}

    @Override
    public void onAdded(@NotNull N network) { /* Do Nothing */ }

    @Override
    public void onRemoved(@NotNull N network) { /* Do Nothing */ }

    @Override
    public void onConnect(@NotNull Loc4 loc) { /* Do Nothing */ }

    @Override
    public void onDisconnect(@NotNull Loc4 loc) { /* Do Nothing */ }

    @Override
    public void refresh() { /* Do Nothing */ }

}
