package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.TestableLevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.api.utility.Loc4;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import javax.annotation.Nonnull;
import java.util.Random;

class NetworkTest {
    static IModule<TestableNetworkNode> module;
    static INetworkManager networkManager;
    static ResourceLocation dimension;

    @BeforeAll
    public static void ClassSetup() {
        module = Module.registerModule("TestNetworkModule", null);
        networkManager = new NetworkManager();
    }

    @AfterAll
    public static void ClassTeardown() {
        Module.clear();
        networkManager.clear();
    }

    @BeforeEach
    public void MethodSetup() {
        dimension = new ResourceLocation(String.valueOf(new Random().nextInt()));
    }

    @AfterEach
    public void MethodTeardown() {
        networkManager.clear();
    }

    public static class TestState {
        @NotNull
        @Nonnull
        TestableNetwork network;
        @NotNull
        @Nonnull
        TestableLevel level;

        public TestState() {
            network = new TestableNetwork(networkManager.getNextID(), module, networkManager);
            network.register();
            level = new TestableLevel(dimension);
        }

        public TestableNetworkNode createNode(BlockPos pos) {
            TestableNetworkNodeBlockEntity e = new TestableNetworkNodeBlockEntity(pos, level);
            TestableNetworkNode node = new TestableNetworkNode(Loc4.of(level, pos));
            e.moduleCapabilityMap.addModule(module, (dir) -> LazyOptional.of(() -> node));
            level.setIBlockEntity(e);
            return node;
        }
    }

    public TestState getState() {
        return new TestState();
    }

    @Test
    void Construct_Empty() {
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        Assertions.assertEquals(0, state.network.getNodes().count());
        Assertions.assertEquals(0, state.network.getConnections().size());
        Assertions.assertEquals(0, state.network.getEdges().count());
    }

    @Test
    void AddNode_AddAndContains() {
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));

        state.network.addNode(node);
        Assertions.assertEquals(1, state.network.size());
        Assertions.assertTrue(state.network.getNodes().anyMatch(node::equals));
    }

    @Test
    void AddNode_Connectable_AddContainAndAddEdge() {
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        Assertions.assertEquals(1, state.network.size());

        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        state.network.addNode(nodeNeighbor);
        Assertions.assertEquals(2, state.network.size());
        Assertions.assertEquals(2, state.network.getNodes().count());
        Assertions.assertEquals(1, state.network.getEdges().count());
        var map = state.network.getConnections();
        Assertions.assertEquals(2, map.size());
        Assertions.assertTrue(map.containsKey(node.getLoc()));
        Assertions.assertTrue(map.containsKey(nodeNeighbor.getLoc()));
        Assertions.assertTrue(map.get(node.getLoc()).stream().anyMatch((l) -> nodeNeighbor.getLoc().equals(l)));
        Assertions.assertTrue(map.get(nodeNeighbor.getLoc()).stream().anyMatch((l) -> node.getLoc().equals(l)));
        var edges = state.network.getEdges().toList();
        Assertions.assertEquals(1, edges.size());
        var testEdge = new NetworkEdge(node.getLoc(), nodeNeighbor.getLoc());
        Assertions.assertTrue(edges.stream().anyMatch(testEdge::equals));
    }

    @Test
    void Create_ShouldCreateLikeNetworkKind() {
        var state = getState();
        var copy = state.network.create();
        Assertions.assertNotNull(copy);
        Assertions.assertInstanceOf(TestableNetwork.class, copy);
    }

    @Test
    void AddNode_Connectable3InLine_AddContainAndAddEdge() {
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        state.network.addNode(nodeNeighbor);
        var nodeNeighbor2 = state.createNode(new BlockPos(2, 0, 0));
        state.network.addNode(nodeNeighbor2);
        Assertions.assertEquals(3, state.network.size());
        Assertions.assertEquals(3, state.network.getNodes().count());
        Assertions.assertEquals(2, state.network.getEdges().count());
        var map = state.network.getConnections();
        Assertions.assertEquals(3, map.size());
        Assertions.assertTrue(map.containsKey(node.getLoc()));
        Assertions.assertTrue(map.containsKey(nodeNeighbor.getLoc()));
        Assertions.assertTrue(map.containsKey(nodeNeighbor2.getLoc()));
        Assertions.assertTrue(map.get(node.getLoc()).stream().anyMatch((l) -> nodeNeighbor.getLoc().equals(l)));
        Assertions.assertTrue(map.get(nodeNeighbor.getLoc()).stream().anyMatch((l) -> node.getLoc().equals(l) || nodeNeighbor2.getLoc().equals(l)));
        Assertions.assertTrue(map.get(nodeNeighbor2.getLoc()).stream().anyMatch((l) -> nodeNeighbor.getLoc().equals(l)));
        var edges = state.network.getEdges().toList();
        Assertions.assertEquals(2, edges.size());
        var testEdge = new NetworkEdge(node.getLoc(), nodeNeighbor.getLoc());
        var testEdge2 = new NetworkEdge(nodeNeighbor.getLoc(), nodeNeighbor2.getLoc());
        Assertions.assertTrue(edges.stream().anyMatch(testEdge::equals));
        Assertions.assertTrue(edges.stream().anyMatch(testEdge2::equals));
    }

    @Test
    void RemoveNode_EndOf3InLine_RemoveNodeAndEdge() {
        // Arrange
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        state.network.addNode(nodeNeighbor);
        var nodeNeighbor2 = state.createNode(new BlockPos(2, 0, 0));
        state.network.addNode(nodeNeighbor2);
        Assertions.assertEquals(3, state.network.size());
        Assertions.assertEquals(3, state.network.getNodes().count());
        Assertions.assertEquals(2, state.network.getEdges().count());
        // Act
        state.network.removeNode(nodeNeighbor2);
        // Assert
        Assertions.assertEquals(2, state.network.size());
        Assertions.assertEquals(2, state.network.getNodes().count());
        Assertions.assertEquals(1, state.network.getEdges().count());
        var map = state.network.getConnections();
        Assertions.assertEquals(2, map.size());
        Assertions.assertTrue(map.containsKey(node.getLoc()));
        Assertions.assertTrue(map.containsKey(nodeNeighbor.getLoc()));
        Assertions.assertFalse(map.containsKey(nodeNeighbor2.getLoc()));
        Assertions.assertTrue(map.get(node.getLoc()).stream().anyMatch((l) -> nodeNeighbor.getLoc().equals(l)));
        Assertions.assertTrue(map.get(nodeNeighbor.getLoc()).stream().allMatch((l) -> node.getLoc().equals(l) && !nodeNeighbor2.getLoc().equals(l)));
        var edges = state.network.getEdges().toList();
        Assertions.assertEquals(1, edges.size());
        var testEdge = new NetworkEdge(node.getLoc(), nodeNeighbor.getLoc());
        var testEdge2 = new NetworkEdge(nodeNeighbor.getLoc(), nodeNeighbor2.getLoc());
        Assertions.assertTrue(edges.stream().anyMatch(testEdge::equals));
        Assertions.assertFalse(edges.stream().anyMatch(testEdge2::equals));
    }

    @Test
    void RemoveNode_MiddleOf3InLine_SplitAndMakeSubnetworks() {
        // Arrange
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        state.network.addNode(nodeNeighbor);
        var nodeNeighbor2 = state.createNode(new BlockPos(2, 0, 0));
        state.network.addNode(nodeNeighbor2);
        Assertions.assertEquals(3, state.network.size());
        Assertions.assertEquals(3, state.network.getNodes().count());
        Assertions.assertEquals(2, state.network.getEdges().count());
        // Act
        state.network.removeNode(nodeNeighbor);
        // Assert
        // Network should split
        Assertions.assertEquals(0, state.network.size());
        Assertions.assertEquals(0, state.network.getNodes().count());
        Assertions.assertEquals(0, state.network.getEdges().count());
        Assertions.assertTrue(networkManager.getNetwork(state.network.ID()).isEmpty());
        // Each node should have its own network
        Assertions.assertNotEquals(node.getNetwork(), state.network);
        Assertions.assertNotEquals(nodeNeighbor2.getNetwork(), state.network);
        Assertions.assertNotEquals(node.getNetwork(), nodeNeighbor2.getNetwork());
        // Each network should consist of only the node and no edges
        Assertions.assertEquals(1, node.getNetwork().size());
        Assertions.assertEquals(0, node.getNetwork().getEdges().count());
        Assertions.assertTrue(node.getNetwork().getNodes().anyMatch(node::equals));
        Assertions.assertTrue(networkManager.getNetwork(node.getNetwork().ID()).isPresent());
        Assertions.assertEquals(1, nodeNeighbor2.getNetwork().size());
        Assertions.assertEquals(0, nodeNeighbor2.getNetwork().getEdges().count());
        Assertions.assertTrue(nodeNeighbor2.getNetwork().getNodes().anyMatch(nodeNeighbor2::equals));
        Assertions.assertTrue(networkManager.getNetwork(nodeNeighbor2.getNetwork().ID()).isPresent());
    }

    @Test
    void RemoveConnection_1From2In3InLine_SplitAndMakeSubnetworks() {
        // Arrange
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        state.network.addNode(nodeNeighbor);
        var nodeNeighbor2 = state.createNode(new BlockPos(2, 0, 0));
        state.network.addNode(nodeNeighbor2);
        Assertions.assertEquals(3, state.network.size());
        Assertions.assertEquals(3, state.network.getNodes().count());
        Assertions.assertEquals(2, state.network.getEdges().count());
        // Act
        state.network.removeConnectionNodes(node, nodeNeighbor);
        // Assert
        // Network should split
        Assertions.assertEquals(0, state.network.size());
        Assertions.assertEquals(0, state.network.getNodes().count());
        Assertions.assertEquals(0, state.network.getEdges().count());
        Assertions.assertTrue(networkManager.getNetwork(state.network.ID()).isEmpty());
        // Each node group should have its own network
        Assertions.assertNotEquals(node.getNetwork(), state.network);
        Assertions.assertNotEquals(nodeNeighbor.getNetwork(), state.network);
        Assertions.assertNotEquals(nodeNeighbor2.getNetwork(), state.network);
        Assertions.assertNotEquals(node.getNetwork(), nodeNeighbor2.getNetwork());
        // Each network should consist of only the node and no edges
        Assertions.assertEquals(1, node.getNetwork().size());
        Assertions.assertEquals(0, node.getNetwork().getEdges().count());
        Assertions.assertTrue(node.getNetwork().getNodes().anyMatch(node::equals));
        Assertions.assertTrue(networkManager.getNetwork(node.getNetwork().ID()).isPresent());

        Assertions.assertEquals(2, nodeNeighbor.getNetwork().size());
        Assertions.assertEquals(1, nodeNeighbor.getNetwork().getEdges().count());
        Assertions.assertTrue(nodeNeighbor2.getNetwork().getNodes().anyMatch(nodeNeighbor::equals));
        Assertions.assertTrue(nodeNeighbor2.getNetwork().getNodes().anyMatch(nodeNeighbor2::equals));
        Assertions.assertTrue(networkManager.getNetwork(nodeNeighbor.getNetwork().ID()).isPresent());
        Assertions.assertEquals(nodeNeighbor.getNetwork(), nodeNeighbor2.getNetwork());
    }

    @Test
    void RemoveNode_MiddleOf4InLine_SplitAndMakeSubnetworks() {
        // Arrange
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        state.network.addNode(nodeNeighbor);
        var nodeNeighbor2 = state.createNode(new BlockPos(2, 0, 0));
        state.network.addNode(nodeNeighbor2);
        var nodeNeighbor3 = state.createNode(new BlockPos(3, 0, 0));
        state.network.addNode(nodeNeighbor3);
        Assertions.assertEquals(4, state.network.size());
        Assertions.assertEquals(4, state.network.getNodes().count());
        Assertions.assertEquals(3, state.network.getEdges().count());
        // Act
        state.network.removeNode(nodeNeighbor);
        // Assert
        // Network should split
        Assertions.assertEquals(0, state.network.size());
        Assertions.assertEquals(0, state.network.getNodes().count());
        Assertions.assertEquals(0, state.network.getEdges().count());
        Assertions.assertTrue(networkManager.getNetwork(state.network.ID()).isEmpty());
        // Each node group should have its own network
        Assertions.assertNotEquals(node.getNetwork(), state.network);
        Assertions.assertNotEquals(nodeNeighbor2.getNetwork(), state.network);
        Assertions.assertNotEquals(node.getNetwork(), nodeNeighbor2.getNetwork());
        // Nodes 2 and 3 should be in the same network
        Assertions.assertEquals(nodeNeighbor2.getNetwork(), nodeNeighbor3.getNetwork());
        // Each network should consist of the remnants of the node group
        Assertions.assertEquals(1, node.getNetwork().size());
        Assertions.assertEquals(0, node.getNetwork().getEdges().count());
        Assertions.assertTrue(node.getNetwork().getNodes().anyMatch(node::equals));
        Assertions.assertTrue(networkManager.getNetwork(node.getNetwork().ID()).isPresent());

        Assertions.assertEquals(2, nodeNeighbor2.getNetwork().size());
        Assertions.assertEquals(1, nodeNeighbor2.getNetwork().getEdges().count());
        Assertions.assertTrue(nodeNeighbor2.getNetwork().getNodes().anyMatch(nodeNeighbor2::equals));
        Assertions.assertTrue(networkManager.getNetwork(nodeNeighbor2.getNetwork().ID()).isPresent());
    }

    @Test
    void AddConnection_TakeoverOfOneNetworkByAnother() {
        // Arrange
        var state = getState();
        var network2 = new TestableNetwork(networkManager.getNextID(), module, networkManager);
        network2.register();
        Assertions.assertEquals(0, state.network.size());
        var node = state.createNode(new BlockPos(0, 0, 0));
        state.network.addNode(node);
        var nodeNeighbor = state.createNode(new BlockPos(1, 0, 0));
        network2.addNode(nodeNeighbor);
        Assertions.assertTrue(networkManager.getNetwork(state.network.ID()).isPresent());
        Assertions.assertEquals(1, state.network.size());
        Assertions.assertEquals(1, state.network.getNodes().count());
        Assertions.assertEquals(0, state.network.getEdges().count());
        Assertions.assertTrue(networkManager.getNetwork(network2.ID()).isPresent());
        Assertions.assertEquals(1, network2.size());
        Assertions.assertEquals(1, network2.getNodes().count());
        Assertions.assertEquals(0, network2.getEdges().count());
        // Act
        state.network.addConnectionNodes(node, nodeNeighbor);
        // Assert
        // Network should takeover
        Assertions.assertSame(state.network, node.getNetwork());
        Assertions.assertSame(node.getNetwork(), nodeNeighbor.getNetwork());
        Assertions.assertTrue(networkManager.getNetwork(node.getNetwork().ID()).isPresent());
        Assertions.assertTrue(networkManager.getNetwork(network2.ID()).isEmpty());
        Assertions.assertEquals(2, state.network.size());
        Assertions.assertEquals(2, state.network.getNodes().count());
        Assertions.assertEquals(1, state.network.getEdges().count());
        var edges = state.network.getEdges().toList();
        Assertions.assertEquals(1, edges.size());
        var testEdge = new NetworkEdge(node.getLoc(), nodeNeighbor.getLoc());
        Assertions.assertTrue(edges.stream().anyMatch(testEdge::equals));
    }
}