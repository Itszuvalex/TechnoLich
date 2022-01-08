package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.utility.ChunkCoord;
import com.itszuvalex.technolich.api.utility.Loc4;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is the primary interface driving my smart BlockEntity networks.
 * This manages wiring, power transfer, and in fact any 'smart connection' logic between multiple sets of BlockEntities.
 * <p>
 * This framework allows for networks that do not have to be BlockPos adjacent, nor even necessarily on the same world.
 * <p>
 * Networks mainly exist to host single-tick update algorithms that are lower complexity than node-based algorithms -
 * see power transfer.
 * If there are P producers and C consumers -
 * node by node, each of those P producers would have to check C consumers to see who they can distribute power to,
 * resulting in P*C complexity.
 * network - knows all P producers and C consumers, by traversing them in some manner, we can visit each node only
 * once, resulting in P+C complexity.
 * For small networks (2 of each) this is exactly equal.  For larger networks (50 each), we visit 100 instead of 250
 * times.
 * For a ridiculous network (150), we visit 300 instead of 22,500.
 * <p>
 * We also get niceties such as network-wide metric tracking.
 * <p>
 * Networks only track BlockEntities that are in loaded chunks.  When a chunk unloads, we have
 * {@link #removeNodes(Stream)} to prevent recalculating
 * subnetworks for each BlockEntity in the chunk.
 * <p>
 * Uses Curious-Recurring-Template-Pattern
 *
 * @param <C> The derived class of the Nodes comprising this network.
 * @param <N> The derived class of the Network
 */
public interface INetwork<C extends INetworkNode<C, N>, N extends INetwork<C, N>> {
    /**
     * @return Network Identifier.  This should be unique.
     */
    int ID();

    /**
     * @return LogicalSide hosting this network. Should mostly be {@link LogicalSide@Server}.
     */
    LogicalSide getSide();

    @Nonnull
    @NotNull
    default N castThis() {return (N) this;}

    /**
     * @return Create an empty new network of this type.
     */
    @NotNull
    @Nonnull
    N create();

    /**
     * @param nodes Nodes to make a new network out of
     * @param edges Edges to include in the network.
     * @return Create a new network of this type from the given collection of nodes.
     */
    @NotNull
    @Nonnull
    N createWithNodesAndEdges(@NotNull @Nonnull Stream<C> nodes, @NotNull @Nonnull Stream<NetworkEdge> edges);

    /**
     * @return All nodes in this network.
     */
    @NotNull
    @Nonnull
    Stream<C> getNodes();

    /**
     * Helper function for getting edges in an easy to parse manner.
     *
     * @return Tuple of all edge pairs.
     */
    @NotNull
    @Nonnull
    Stream<NetworkEdge> getEdges();

    /**
     * Helper function for getting connections in an easy to parse manner.
     * Connections, due to how its formatted.
     *
     * @return All connections, mapped by location.
     */
    @Nonnull
    @NotNull
    Map<Loc4, Set<Loc4>> getConnections();

    /**
     * @param loc Location to get connections for
     * @return Optional.empty() if loc not tracked, otherwise a stream containing all locations the loc is connected to.
     */
    @NotNull
    @Nonnull
    Optional<Stream<Loc4>> getConnections(Loc4 loc);

    boolean canConnectNodes(@NotNull @Nonnull C a, @NotNull @Nonnull C b);

    boolean canConnectLocs(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b);

    void addConnectionNodes(@NotNull @Nonnull C a, @NotNull @Nonnull C b);

    void addConnectionLocs(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b);

    void removeConnectionNodes(@NotNull @Nonnull C a, @NotNull @Nonnull C b);

    void removeConnectionLocs(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b);

    boolean canAddNode(@NotNull @Nonnull C node);

    void addNode(@NotNull @Nonnull C node);

    void removeNode(@NotNull @Nonnull C node);

    void removeNodes(@NotNull @Nonnull Stream<C> nodes);

    /**
     * Called when a node is removed from the network.  Maps all out all sub-networks created by the split, creates
     * and registers them, and informs nodes.
     *
     * @param edges All nodes that were connected to all nodes that were removed.
     */
    void split(@NotNull @Nonnull Collection<Loc4> edges);

    /**
     * Called on sub networks by a main network, when that network is splitting apart.
     *
     * @param network Network that will split into this sub network.
     */
    void onSplit(@NotNull @Nonnull N network);

    /**
     * Called when a node is added to the network.  Sets ownership of all of its nodes to this one, takes over
     * connections.
     *
     * @param network Network that this network is taking over.
     */
    void takeover(@NotNull @Nonnull N network);

    /**
     * Called on networks by another network, when that network is incorporating this network.
     *
     * @param network Network that is taking over this network.
     */
    void onTakeover(@NotNull @Nonnull N network);

    /**
     * Simply remove all nodes from the network.  Does not inform them.
     */
    void clear();

    /**
     * Orders all nodes to refresh.
     */
    void refresh();

    /**
     * Register this network with the Network Manager.  Starts tick updates.
     */
    void register();

    /**
     * Unregister this network from the Network Manager.  Stops tick updates.
     */
    void unregister();

    /**
     * @return Number of nodes in the network.
     */
    int size();

    /**
     * Called when a tick starts.
     */
    void onTickStart();

    /**
     * Called when a tick ends.
     */
    void onTickEnd();

    /**
     * Called by TileNetwork when a chunk unloads to allow for more efficient network splitting
     *
     * @param chunk Coordinates of chunk unloading
     */
    void onChunkUnload(ILevel level, ChunkCoord chunk);
}
