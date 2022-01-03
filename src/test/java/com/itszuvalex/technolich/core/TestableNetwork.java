package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TestableNetwork extends TileNetwork<TestableNetworkNode, TestableNetwork> {
    private final @NotNull
    @Nonnull
    IModule<TestableNetworkNode> module;
    private final @NotNull
    @Nonnull
    INetworkManager manager;

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
}
