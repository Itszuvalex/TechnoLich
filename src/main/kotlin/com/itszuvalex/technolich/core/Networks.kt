package com.itszuvalex.technolich.core

import com.itszuvalex.technolich.TechnoLich
import com.itszuvalex.technolich.api.adapters.ILevel
import com.itszuvalex.technolich.api.adapters.IModule
import com.itszuvalex.technolich.api.utility.ChunkCoord
import com.itszuvalex.technolich.api.utility.Loc4
import com.itszuvalex.technolich.api.utility.LocationTracker
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.fml.LogicalSide
import net.neoforged.neoforge.server.ServerLifecycleHooks
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicInteger

/**
 * The primary interface driving smart block entity networks: wiring, power transfer, and any 'smart connection' logic
 * between multiple sets of block entities.
 *
 * Networks need not be BlockPos adjacent, nor even in the same world.
 *
 * Networks mainly exist to host single-tick update algorithms that are lower complexity than node-based algorithms,
 * e.g. power transfer. With P producers and C consumers, node by node each producer checks every consumer (P*C); the
 * network knows all of them and can visit each node once (P+C).
 *
 * Networks only track block entities in loaded chunks. When a chunk unloads, [onChunkUnload] removes its nodes as a
 * batch (see [removeNodes]) instead of recalculating sub-networks per block entity.
 *
 * Uses the Curiously-Recurring-Template-Pattern.
 *
 * @param C The derived class of the Nodes comprising this network.
 * @param N The derived class of the Network
 */
interface INetwork<C : INetworkNode<C, N>, N : INetwork<C, N>> {
    /**
     * Network identifier. This should be unique.
     */
    val id: Int

    /**
     * LogicalSide hosting this network. Should mostly be [LogicalSide.SERVER].
     */
    val side: LogicalSide

    @Suppress("UNCHECKED_CAST")
    fun castThis(): N = this as N

    /**
     * @return Create an empty new network of this type.
     */
    fun create(): N

    /**
     * @return A new network of this type from the given nodes and edges.
     */
    fun createWithNodesAndEdges(nodes: Sequence<C>, edges: Sequence<NetworkEdge>): N

    val nodes: Sequence<C>

    /**
     * @return Every edge once, as (smaller, larger) location pairs.
     */
    val edges: Sequence<NetworkEdge>

    /**
     * @return All connections, mapped by location.
     */
    val connections: Map<Loc4, Set<Loc4>>

    /**
     * @return null if loc is not tracked, otherwise the locations it is connected to.
     */
    fun connectionsOf(loc: Loc4): Sequence<Loc4>?

    fun canConnectNodes(a: C, b: C): Boolean

    fun canConnectLocs(a: Loc4, b: Loc4): Boolean

    fun addConnectionNodes(a: C, b: C)

    fun addConnectionLocs(a: Loc4, b: Loc4)

    fun removeConnectionNodes(a: C, b: C)

    fun removeConnectionLocs(a: Loc4, b: Loc4)

    fun canAddNode(node: C): Boolean

    fun addNode(node: C)

    fun removeNode(node: C)

    fun removeNodes(nodes: Sequence<C>)

    /**
     * Called when a node is removed from the network. Maps out all sub-networks created by the split, creates and
     * registers them, and informs nodes.
     *
     * @param edges All nodes that were connected to the nodes that were removed.
     */
    fun split(edges: Collection<Loc4>)

    /**
     * Called on sub networks by a main network, when that network is splitting apart.
     */
    fun onSplit(network: N)

    /**
     * Takes ownership of all of [network]'s nodes and connections.
     */
    fun takeover(network: N)

    /**
     * Called on networks by another network, when that network is incorporating this network.
     */
    fun onTakeover(network: N)

    /**
     * Simply remove all nodes from the network. Does not inform them.
     */
    fun clear()

    /**
     * Orders all nodes to refresh.
     */
    fun refresh()

    /**
     * Register this network with the Network Manager. Starts tick updates.
     */
    fun register()

    /**
     * Unregister this network from the Network Manager. Stops tick updates.
     */
    fun unregister()

    val size: Int

    fun onTickStart()

    fun onTickEnd()

    /**
     * Removes the nodes in an unloading chunk as a batch.
     */
    fun onChunkUnload(level: ILevel, chunk: ChunkCoord)
}

/**
 * Uses the Curiously-Recurring-Template-Pattern.
 *
 * @param C The derived class of the Nodes comprising this network.
 * @param N The derived class of the Network
 */
interface INetworkNode<C : INetworkNode<C, N>, N : INetwork<C, N>> {
    /**
     * The network this node is in, or null before it has been added to one.
     */
    var network: N?

    val loc: Loc4

    fun refresh()

    fun canConnect(loc: Loc4): Boolean

    fun canAdd(network: N): Boolean

    fun onAdded(network: N)

    fun onRemoved(network: N)

    fun onConnect(loc: Loc4)

    fun onDisconnect(loc: Loc4)
}

data class NetworkEdge(val a: Loc4, val b: Loc4)

interface INetworkManager {
    fun getNetwork(id: Int): INetwork<*, *>?

    fun removeNetwork(network: INetwork<*, *>)

    fun addNetwork(network: INetwork<*, *>)

    val networkCount: Int

    val networks: Sequence<INetwork<*, *>>

    fun nextId(): Int

    fun clear()

    fun onTickEnd()

    fun onTickStart()

    fun onChunkUnload(level: ILevel, chunk: ChunkCoord)
}

class NetworkManager : INetworkManager {
    private val nextID = AtomicInteger(0)
    private val networkMap = HashMap<Int, INetwork<*, *>>()

    override fun getNetwork(id: Int): INetwork<*, *>? = networkMap[id]

    override fun removeNetwork(network: INetwork<*, *>) {
        networkMap.remove(network.id)
    }

    override fun addNetwork(network: INetwork<*, *>) {
        networkMap.putIfAbsent(network.id, network)
    }

    override val networkCount: Int get() = networkMap.size

    override val networks: Sequence<INetwork<*, *>> get() = networkMap.values.asSequence()

    override fun nextId(): Int = nextID.getAndIncrement()

    override fun clear() = networkMap.clear()

    // Snapshot: callbacks may add or remove networks (splits, takeovers).
    override fun onTickEnd() = networkMap.values.toList().forEach { it.onTickEnd() }

    override fun onTickStart() = networkMap.values.toList().forEach { it.onTickStart() }

    override fun onChunkUnload(level: ILevel, chunk: ChunkCoord) =
        networkMap.values.toList().forEach { it.onChunkUnload(level, chunk) }
}

/**
 * A network of block entities, found through [networkModule] on the block entity at each location.
 */
abstract class TileNetwork<C : INetworkNode<C, N>, N : TileNetwork<C, N>>(override val id: Int, override val side: LogicalSide) :
    INetwork<C, N> {
    private val nodeMap = HashMap<Loc4, C>()
    private val connectionMap = HashMap<Loc4, MutableSet<Loc4>>()
    private val locationTracker = LocationTracker()

    abstract fun networkModule(): IModule<C>

    override fun createWithNodesAndEdges(nodes: Sequence<C>, edges: Sequence<NetworkEdge>): N {
        val net = create()
        nodes.forEach { net.addNodeSilently(it) }
        edges.forEach { net.addConnectionSilently(it.a, it.b) }
        return net
    }

    override val nodes: Sequence<C> get() = nodeMap.values.asSequence()

    override val edges: Sequence<NetworkEdge> get() = connectionMap.asSequence().flatMap { (key, to) ->
        to.asSequence().filter { key < it }.map { NetworkEdge(key, it) }
    }

    override val connections: Map<Loc4, Set<Loc4>> get() = connectionMap

    override fun connectionsOf(loc: Loc4): Sequence<Loc4>? = connectionMap[loc]?.asSequence()

    override fun canConnectNodes(a: C, b: C): Boolean = a.canConnect(b.loc) && b.canConnect(a.loc)

    override fun canConnectLocs(a: Loc4, b: Loc4): Boolean {
        val aMod = getModForLoc(a) ?: return false
        val bMod = getModForLoc(b) ?: return false
        return canConnectNodes(aMod, bMod)
    }

    override fun addConnectionNodes(a: C, b: C) {
        addConnectionSilently(a.loc, b.loc)
        addConnectionInternal(a, b)
    }

    override fun addConnectionLocs(a: Loc4, b: Loc4) {
        val aMod = getModForLoc(a) ?: return
        val bMod = getModForLoc(b) ?: return
        addConnectionSilently(a, b)
        addConnectionInternal(aMod, bMod)
    }

    override fun removeConnectionNodes(a: C, b: C) {
        val aLoc = a.loc
        val bLoc = b.loc
        removeConnectionsSilently(aLoc, bLoc)
        a.onDisconnect(bLoc)
        b.onDisconnect(aLoc)
        split(listOf(aLoc, bLoc))
    }

    override fun removeConnectionLocs(a: Loc4, b: Loc4) {
        removeConnectionBatch(a, b)
        split(listOf(a, b))
    }

    override fun canAddNode(node: C): Boolean = true

    /**
     * Adds the node, first removing it from any other network it belongs to, then connects it to every node here it
     * can connect to. No-op if the node is already in this network.
     */
    override fun addNode(node: C) {
        if (nodeMap[node.loc] === node) return
        if (!canAddNode(node)) return
        if (!node.canAdd(castThis())) return
        val previous = node.network
        if (previous != null && previous !== this) previous.removeNode(node)
        addNodeSilently(node)
        node.onAdded(castThis())
        nodes.filter { it !== node && canConnectNodes(node, it) }.toList().forEach { addConnectionNodes(it, node) }
    }

    override fun removeNode(node: C) = removeNodes(sequenceOf(node))

    /**
     * Removes the nodes as a batch, calls [INetworkNode.onRemoved] on each, then splits whatever is left.
     */
    override fun removeNodes(nodes: Sequence<C>) {
        val nodeLocSet = nodes.map { it.loc }.toHashSet()
        if (nodeLocSet.isEmpty()) return

        // Locations of all nodes connected to a removed node, excluding removed nodes.
        val edges = nodeLocSet.asSequence().mapNotNull { connectionsOf(it) }.flatten().toHashSet()
        edges.removeAll(nodeLocSet)

        val removed = ArrayList<C>()
        nodeLocSet.forEach { a ->
            // Realize the list, as we're about to modify the underlying structure.
            connectionsOf(a)?.toList()?.forEach { c -> removeConnectionBatch(a, c) }
            nodeMap.remove(a)?.let(removed::add)
            locationTracker.removeLocation(a)
        }
        removed.forEach { it.onRemoved(castThis()) }

        split(edges)

        if (size == 0) {
            clear()
            unregister()
        }
    }

    override fun split(edges: Collection<Loc4>) {
        val workingSet = HashSet(edges)
        val networks = ArrayList<Set<Loc4>>()
        while (workingSet.isNotEmpty()) {
            val nodes = NetworkExplorer.explore(workingSet.first(), this)
            networks.add(nodes)
            workingSet.removeAll(nodes)
        }

        // Only split if necessary
        if (networks.size <= 1) return

        val edgeTuples = this.edges.toHashSet()
        networks.forEach { networkNodes ->
            val network = createWithNodesAndEdges(
                networkNodes.asSequence().mapNotNull { nodeMap[it] },
                edgeTuples.asSequence().filter { it.a in networkNodes },
            )
            network.onSplit(castThis())
            network.register()
        }

        clear()
        unregister()
    }

    override fun onSplit(network: N) {}

    override fun takeover(network: N) {
        network.onTakeover(castThis())
        network.nodes.toList().forEach { addNodeSilently(it) }
        network.edges.toList().forEach { addConnectionSilently(it.a, it.b) }
        network.clear()
        network.unregister()
    }

    override fun onTakeover(network: N) {}

    override fun clear() {
        nodeMap.clear()
        connectionMap.clear()
        locationTracker.clear()
    }

    override fun refresh() = nodes.forEach { it.refresh() }

    override fun register() {
        TechnoLich.NETWORK_MANAGER.get(side)?.addNetwork(this)
    }

    override fun unregister() {
        TechnoLich.NETWORK_MANAGER.get(side)?.removeNetwork(this)
    }

    override val size: Int get() = nodeMap.size

    override fun onTickStart() {}

    override fun onTickEnd() {}

    override fun onChunkUnload(level: ILevel, chunk: ChunkCoord) {
        removeNodes(locationTracker.getTrackedLocationsInChunk(level.dimensionId, chunk).toList().asSequence().mapNotNull { nodeMap[it] })
    }

    /**
     * The level for [dimension], used to find the block entity at a [Loc4]: networks can span dimensions, so a
     * location alone does not say which level to look in. Defaults to the running server's level; tests override it.
     */
    protected open fun levelFor(dimension: Identifier): ILevel? =
        ServerLifecycleHooks.getCurrentServer()?.getLevel(ResourceKey.create(Registries.DIMENSION, dimension))?.let(ILevel::of)

    private fun getModForLoc(loc: Loc4): C? =
        levelFor(loc.dimensionId)?.let { loc.getIBlockEntity(it) }?.getModule(networkModule(), null)

    private fun addConnectionSilently(a: Loc4, b: Loc4) {
        connectionMap.getOrPut(a, ::HashSet).add(b)
        connectionMap.getOrPut(b, ::HashSet).add(a)
    }

    /**
     * Pulls both nodes (and their networks) into this network, then informs them of the connection.
     */
    private fun addConnectionInternal(a: C, b: C) {
        absorb(a)
        absorb(b)
        a.onConnect(b.loc)
        b.onConnect(a.loc)
    }

    private fun absorb(node: C) {
        val network = node.network
        if (network === this) return
        if (network == null) {
            addNodeSilently(node)
            node.onAdded(castThis())
        } else {
            takeover(network)
        }
    }

    private fun removeConnectionBatch(a: Loc4, b: Loc4) {
        removeConnectionsSilently(a, b)
        getModForLoc(a)?.onDisconnect(b)
        getModForLoc(b)?.onDisconnect(a)
    }

    private fun removeConnectionsSilently(a: Loc4, b: Loc4) {
        removeAndCleanupConnection(a, b)
        removeAndCleanupConnection(b, a)
    }

    private fun removeAndCleanupConnection(a: Loc4, b: Loc4) {
        val set = connectionMap[a] ?: return
        set.remove(b)
        if (set.isEmpty()) connectionMap.remove(a)
    }

    private fun addNodeSilently(node: C) {
        nodeMap[node.loc] = node
        node.network = castThis()
        locationTracker.trackLocation(node.loc)
    }

    object NetworkExplorer {
        /**
         * @return Every location reachable from start through the network's connections, including start.
         * Iterative, so long chains (e.g. cables) can't overflow the stack.
         */
        @JvmStatic
        fun explore(start: Loc4, network: TileNetwork<*, *>): HashSet<Loc4> {
            val explored = hashSetOf(start)
            val frontier = ArrayDeque<Loc4>().apply { add(start) }
            while (frontier.isNotEmpty()) {
                network.connectionsOf(frontier.poll())?.forEach { if (explored.add(it)) frontier.add(it) }
            }
            return explored
        }
    }
}

abstract class TileNetworkNode<C : TileNetworkNode<C, N>, N : TileNetwork<C, N>> : INetworkNode<C, N> {
    override var network: N? = null

    override fun canConnect(loc: Loc4): Boolean = this.loc.isNeighbor(loc)

    override fun canAdd(network: N): Boolean = true

    override fun onAdded(network: N) {}

    override fun onRemoved(network: N) {}

    override fun onConnect(loc: Loc4) {}

    override fun onDisconnect(loc: Loc4) {}

    override fun refresh() {}
}
