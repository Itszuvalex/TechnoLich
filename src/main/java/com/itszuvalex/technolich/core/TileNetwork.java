package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.ChunkCoord;
import com.itszuvalex.technolich.api.utility.FunctionalHelpers;
import com.itszuvalex.technolich.api.utility.Loc4;
import com.itszuvalex.technolich.api.utility.LocationTracker;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public abstract class TileNetwork<C extends INetworkNode<C, N>, N extends TileNetwork<C, N>> implements INetwork<C, N> {
    private @NotNull
    @Nonnull
    final
    HashMap<Loc4, C> nodeMap;
    private @NotNull
    @Nonnull
    final
    HashMap<Loc4, Set<Loc4>> connectionMap;
    private @NotNull
    @Nonnull
    final LocationTracker locationTracker;

    private final int ID;
    private final LogicalSide side;

    public TileNetwork(int id, LogicalSide side) {
        ID = id;
        this.side = side;
        nodeMap = new HashMap<>();
        connectionMap = new HashMap<>();
        locationTracker = new LocationTracker();
    }

    public abstract IModule<C> networkModule();

    @Override
    public int ID() {
        return ID;
    }

    @Override
    public LogicalSide getSide() {return side;}

    @Override
    public @NotNull N createWithNodesAndEdges(@NotNull Stream<C> nodes, @NotNull Stream<NetworkEdge> edges) {
        N net = create();
        nodes.forEach((n) -> ((TileNetwork<C, N>) net).addNodeSilently(n));
        edges.forEach((e) -> ((TileNetwork<C, N>) net).addConnectionSilently(e.a(), e.b()));
        return net;
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
        return connectionMap;
    }

    @Override
    public @NotNull Optional<Stream<Loc4>> getConnections(Loc4 loc) {
        if (!connectionMap.containsKey(loc)) {return Optional.empty();}
        return Optional.of(connectionMap.get(loc).stream());
    }

    @Override
    public boolean canConnectNodes(@NotNull C a, @NotNull C b) {
        return a.canConnect(b.getLoc()) && b.canConnect(a.getLoc());
    }

    @Override
    public boolean canConnectLocs(@NotNull Loc4 a, @NotNull Loc4 b) {
        var aBEmodOpt = getModForLoc(a);
        if (aBEmodOpt.isEmpty()) return false;
        var aBEmod = aBEmodOpt.get();

        var bBEmodOpt = getModForLoc(b);
        if (bBEmodOpt.isEmpty()) return false;
        var bBEmod = bBEmodOpt.get();

        return canConnectNodes(aBEmod, bBEmod);
    }

    @Override
    public void addConnectionNodes(@NotNull C a, @NotNull C b) {
        addConnectionSilently(a.getLoc(), b.getLoc());
        addConnectionInternal(a, b);
    }

    @Override
    public void addConnectionLocs(@NotNull Loc4 a, @NotNull Loc4 b) {
        var aModOpt = getModForLoc(a);
        if (aModOpt.isEmpty()) return;
        var aMod = aModOpt.get();

        var bModOpt = getModForLoc(a);
        if (bModOpt.isEmpty()) return;
        var bMod = bModOpt.get();

        addConnectionSilently(a, b);
        addConnectionInternal(aMod, bMod);
    }

    @Override
    public void removeConnectionNodes(@NotNull C a, @NotNull C b) {
        var aLoc = a.getLoc();
        var bLoc = b.getLoc();
        removeConnectionsSilently(aLoc, bLoc);
        a.onDisconnect(bLoc);
        b.onDisconnect(aLoc);
        split(List.of(aLoc, bLoc));
    }

    @Override
    public void removeConnectionLocs(@NotNull Loc4 a, @NotNull Loc4 b) {
        removeConnectionBatch(a, b);
        split(List.of(a, b));
    }

    @Override
    public boolean canAddNode(@NotNull C node) {
        return true;
    }

    @Override
    public void addNode(@NotNull C node) {
        if (!canAddNode(node)) return;
        if (!node.canAdd(castThis())) return;
        addNodeSilently(node);
        node.onAdded(castThis());
        getNodes().filter((c) -> canConnectNodes(node, c)).forEach((a) -> addConnectionNodes(a, node));
    }

    @Override
    public void removeNode(@NotNull C node) {
        removeNodes(Stream.of(node));
    }

    @Override
    public void removeNodes(@NotNull Stream<C> nodes) {
        List<Loc4> nodeLocs = nodes.map(C::getLoc).toList();

        if(nodeLocs.size() == 0) return; // Skip all this if we're not removing anything.

        // Loc4s of all nodes being removed
        HashSet<Loc4> nodeLocSet = new HashSet<>(nodeLocs);
        // Find edges - set of Loc4s of all nodes connected to a node in nodeLocSet, but not any Loc4
        // in nodeLocSet
        HashSet<Loc4> edges =
                new HashSet<>(nodeLocs.stream().map(this::getConnections).filter(Optional::isPresent).flatMap(Optional::get).toList());
        edges.removeAll(nodeLocSet);

        // Remove connections as batch
        nodeLocSet.forEach((a) -> {
            getConnections(a)
                    .map(Stream::toList) // Realize the Stream, as we're about to modify the underlying structure.
                    .ifPresent((b) -> b
                            .forEach((c) -> removeConnectionBatch(a, c)));
            nodeMap.remove(a);
            locationTracker.removeLocation(a);
        });

        split(edges);

        // Clear ourself if empty
        // TODO: This is duplicated if we did in fact split at all.
        if (size() == 0) {
            clear();
            unregister();
        }
    }

    @Override
    public void split(@NotNull Collection<Loc4> edges) {
        HashSet<Loc4> workingSet = new HashSet<>(edges);
        ArrayList<Collection<Loc4>> networks = new ArrayList<>();
        while (!workingSet.isEmpty()) {
            var first = workingSet.stream().findFirst().get();
            var nodes = NetworkExplorer.explore(first, this);
            networks.add(nodes);
            workingSet.removeAll(nodes);
        }

        // Only split if necessary
        if (networks.size() <= 1) return;

        HashSet<NetworkEdge> edgeTuples = new HashSet<>(getEdges().toList());
        networks.forEach((networkNodes) -> {
            var nodes = networkNodes.stream().map(nodeMap::get);
            var edgesSet = edgeTuples.stream().filter((e) -> networkNodes.contains(e.a()));
            var network = createWithNodesAndEdges(nodes, edgesSet);
            network.onSplit(castThis());
            network.register();
        });

        clear();
        unregister();
    }

    @Override
    public void onSplit(@NotNull N network) {
        // Do nothing
    }

    @Override
    public void takeover(@NotNull N network) {
        network.getNodes().forEach(this::addNodeSilently);
        network.getEdges().forEach((c) -> addConnectionSilently(c.a(), c.b()));
        network.clear();
        network.unregister();
    }

    @Override
    public void onTakeover(@NotNull N network) {
        // Do nothing
    }

    @Override
    public void clear() {
        nodeMap.clear();
        connectionMap.clear();
    }

    @Override
    public void refresh() {
        getNodes().forEach(C::refresh);
    }

    @Override
    public void register() {
        TechnoLich.NETWORK_MANAGER.get(getSide()).ifPresent((a) -> a.addNetwork(this));
    }

    @Override
    public void unregister() {
        TechnoLich.NETWORK_MANAGER.get(getSide()).ifPresent((a) -> a.removeNetwork(this));
    }

    @Override
    public int size() {
        return nodeMap.size();
    }

    @Override
    public void onTickStart() {
        // Do nothing
    }

    @Override
    public void onTickEnd() {
        // Do nothing
    }

    @Override
    public void onChunkUnload(ILevel level, ChunkCoord chunk) {
        removeNodes(
                locationTracker.getTrackedLocationsInChunk(level.dimensionLocation(), chunk).map(nodeMap::get)
        );
    }

    private @NotNull
    @Nonnull
    Optional<C> getModForLoc(@NotNull @Nonnull Loc4 loc) {
        var BEopt = loc.getIBlockEntity(false);
        if (BEopt.isEmpty()) return Optional.empty();

        var BE = BEopt.get();
        var BEmodLazyOpt = BE.getModule(networkModule(), null);
        if (!BEmodLazyOpt.isPresent()) return Optional.empty();
        return BEmodLazyOpt.resolve();
    }

    private void addConnectionSilently(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b) {
        FunctionalHelpers.getOrElseUpdate(connectionMap, a, HashSet::new).add(b);
        FunctionalHelpers.getOrElseUpdate(connectionMap, b, HashSet::new).add(a);
    }

    private void addConnectionInternal(@NotNull @Nonnull C a, @NotNull @Nonnull C b) {
        if (a.getNetwork() != b.getNetwork()) {
            if (a.getNetwork() == this) {
                takeover(b.getNetwork());
            } else {
                takeover(a.getNetwork());
            }
        }
        a.onConnect(b.getLoc());
        b.onConnect(a.getLoc());
    }

    private void removeConnectionBatch(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b) {
        removeConnectionsSilently(a, b);
        getModForLoc(a).ifPresent((c) -> c.onDisconnect(b));
        getModForLoc(b).ifPresent((c) -> c.onDisconnect(a));
    }

    private void removeConnectionsSilently(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b) {
        removeAndCleanupConnection(a, b);
        removeAndCleanupConnection(b, a);
    }

    private void removeAndCleanupConnection(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b) {
        if (!connectionMap.containsKey(a)) {return;}

        var set = connectionMap.get(a);
        set.remove(b);
        if (set.isEmpty()) {connectionMap.remove(a);}
    }

    private void addNodeSilently(@NotNull @Nonnull C node) {
        nodeMap.put(node.getLoc(), node);
        node.setNetwork(castThis());
        locationTracker.trackLocation(node.getLoc());
    }

    public static class NetworkExplorer {
        public static <C extends INetworkNode<C, N>, N extends TileNetwork<C, N>>
        @NotNull HashSet<Loc4> explore(@NotNull @Nonnull Loc4 start, @NotNull @Nonnull TileNetwork<C, N> network) {
            return expandLoc(start, network, new HashSet<>());
        }

        public static <C extends INetworkNode<C, N>, N extends TileNetwork<C, N>> HashSet<Loc4>
        expandLoc(@NotNull @Nonnull Loc4 start, @NotNull @Nonnull TileNetwork<C, N> network,
                  @NotNull @Nonnull HashSet<Loc4> explored) {
            if (explored.contains(start)) {
                return explored;
            }
            explored.add(start);
            network.getConnections(start).ifPresent((i) -> i.forEach((j) -> expandLoc(j, network, explored)));
            return explored;
        }
    }
}
