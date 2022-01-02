package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.Loc4;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public abstract class TileNetwork<C extends INetworkNode<C, N>, N extends TileNetwork<C, N>> implements INetwork<C, N> {
    private @NotNull
    @Nonnull
    HashMap<Loc4, C> nodeMap;
    private @NotNull
    @Nonnull
    HashMap<Loc4, Set<Loc4>> connectionmap;

    private int ID;

    public TileNetwork(int id) {
        ID = id;
        nodeMap = new HashMap<>();
        connectionmap = new HashMap<>();
    }

    public abstract IModule<C> networkModule();

    @Override
    public int ID() {
        return ID;
    }

    @Override
    public @NotNull Stream<C> getNodes() {
        return nodeMap.values().stream();
    }

    @Override
    public @NotNull Stream<NetworkEdge> getEdges() {
        var cons = getConnections();
        return cons.keySet().stream().flatMap((key) -> {
            var to = cons.get(key).stream();
            return to.filter((i) -> key.compareTo(i) < 0).map((i) -> new NetworkEdge(key, i));
        });
    }

    @Override
    public @NotNull Map<Loc4, Set<Loc4>> getConnections() {
        return connectionmap;
    }

    @Override
    public @NotNull Optional<Stream<Loc4>> getConnections(Loc4 loc) {
        return Optional.empty();
    }

    @Override
    public boolean canConnectNodes(@NotNull C a, @NotNull C b) {
        return false;
    }

    @Override
    public boolean canConnectLocs(@NotNull Loc4 a, @NotNull Loc4 b) {
        return false;
    }

    @Override
    public void addConnectionNodes(@NotNull C a, @NotNull C b) {

    }

    @Override
    public void addConnectionLocs(@NotNull Loc4 a, @NotNull Loc4 b) {

    }

    @Override
    public void removeConnectionNodes(@NotNull C a, @NotNull C b) {

    }

    @Override
    public void removeConnectionLocs(@NotNull Loc4 a, @NotNull Loc4 b) {

    }

    @Override
    public boolean canAddNode(@NotNull C node) {
        return false;
    }

    @Override
    public void addNode(@NotNull C node) {

    }

    @Override
    public void removeNode(@NotNull C node) {

    }

    @Override
    public void removeNodes(@NotNull Stream<C> nodes) {

    }

    @Override
    public void split(@NotNull Stream<Loc4> edges) {

    }

    @Override
    public void onSplit(@NotNull N network) {

    }

    @Override
    public void takeover(@NotNull N network) {

    }

    @Override
    public void onTakeover(@NotNull N network) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void onTickStart() {

    }

    @Override
    public void onTickEnd() {

    }

    public static class NetworkExplorer {
        public <C extends INetworkNode<C, N>, N extends TileNetwork<C, N>>
        @NotNull HashSet<Loc4> explore(@NotNull @Nonnull Loc4 start, @NotNull @Nonnull TileNetwork<C, N> network) {
            return expandLoc(start, network, new HashSet<>());
        }

        public <C extends INetworkNode<C, N>, N extends TileNetwork<C, N>> HashSet<Loc4>
        expandLoc(@NotNull @Nonnull Loc4 start, @NotNull @Nonnull TileNetwork<C, N> network, @NotNull @Nonnull HashSet<Loc4> explored) {
            if (explored.contains(start)) {
                return explored;
            }
            explored.add(start);
            network.getConnections(start).ifPresent((i) -> i.forEach((j) -> expandLoc(j, network, explored)));
            return explored;
        }
    }
}
