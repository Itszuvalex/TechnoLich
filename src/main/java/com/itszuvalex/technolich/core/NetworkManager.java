package com.itszuvalex.technolich.core;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class NetworkManager implements INetworkManager {
    private final @NotNull
    @Nonnull
    AtomicInteger nextID;
    private final @NotNull
    @Nonnull
    Map<Integer, INetwork<?, ?>> networkMap;

    public NetworkManager() {
        nextID = new AtomicInteger(0);
        networkMap = new HashMap<>();
    }

    @Override
    public Optional<INetwork<?, ?>> getNetwork(int id) {
        return networkMap.containsKey(id) ? Optional.of(networkMap.get(id)) : Optional.empty();
    }

    @Override
    public void removeNetwork(@NotNull INetwork<?, ?> network) {
        networkMap.remove(network.ID());
    }

    @Override
    public void addNetwork(@NotNull INetwork<?, ?> network) {
        var id = network.ID();
        if (!networkMap.containsKey(id))
            networkMap.put(id, network);
    }

    @Override
    public int networkCount() {
        return networkMap.size();
    }

    @Override
    public @NotNull Stream<INetwork<?, ?>> getNetworks() {
        return networkMap.values().stream();
    }

    @Override
    public int getNextID() {
        return nextID.getAndIncrement();
    }

    @Override
    public void clear() {
        networkMap.clear();
    }

    @Override
    public void onTickEnd() {
        networkMap.values().forEach(INetwork::onTickEnd);
    }

    @Override
    public void onTickStart() {
        networkMap.values().forEach(INetwork::onTickStart);
    }
}
