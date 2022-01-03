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
}