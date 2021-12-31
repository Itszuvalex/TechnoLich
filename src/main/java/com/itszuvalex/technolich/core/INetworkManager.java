package com.itszuvalex.technolich.core;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

public interface INetworkManager {
    Optional<INetwork<?, ?>> getNetwork(int id);

    void removeNetwork(@NotNull @Nonnull INetwork<?, ?> network);

    void addNetwork(@NotNull @Nonnull INetwork<?, ?> network);

    int networkCount();

    @NotNull
    @Nonnull
    Stream<INetwork<?, ?>> getNetworks();

    int getNextID();

    void clear();

    void onTickEnd();

    void onTickStart();
}
