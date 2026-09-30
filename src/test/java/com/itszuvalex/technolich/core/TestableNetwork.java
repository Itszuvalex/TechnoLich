package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TestableNetwork extends TileNetwork<TestableNetworkNode, TestableNetwork> {
    private final @NotNull
    @Nonnull
    IModule<TestableNetworkNode> module;
    private final @NotNull
    @Nonnull
    INetworkManager manager;
    /**
     * The network that took this one over, via onTakeover.
     */
    public TestableNetwork takenOverBy;

    public TestableNetwork(int ID, @NotNull @Nonnull IModule<TestableNetworkNode> module,
                           @NotNull @Nonnull INetworkManager manager) {
        super(ID, LogicalSide.SERVER);
        this.module = module;
        this.manager = manager;
    }

    @Override
    public @NotNull TestableNetwork create() {
        return new TestableNetwork(manager.getNextID(), module, manager);
    }

    @Override
    public IModule<TestableNetworkNode> networkModule() {
        return module;
    }

    @Override
    public void register() {
        manager.addNetwork(this);
    }

    @Override
    public void unregister() {
        manager.removeNetwork(this);
    }

    @Override
    public void onTakeover(@NotNull TestableNetwork network) {
        takenOverBy = network;
    }
}
