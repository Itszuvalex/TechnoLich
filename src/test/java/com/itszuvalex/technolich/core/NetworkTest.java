package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.TestableLevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        dimension = new ResourceLocation(String.valueOf(new Random().nextInt()));
    }

    @AfterAll
    public static void ClassTeardown() {
        Module.clear();
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
    }

    public TestState getState() {return new TestState();}

    @Test
    void Construct_Empty() {
        var state = getState();
        Assertions.assertEquals(0, state.network.size());
        Assertions.assertEquals(0, state.network.getNodes().count());
        Assertions.assertEquals(0, state.network.getConnections().size());
        Assertions.assertEquals(0, state.network.getEdges().count());
    }
}