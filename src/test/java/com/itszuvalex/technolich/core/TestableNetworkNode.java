package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.Loc4;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.annotation.Testable;

import javax.annotation.Nonnull;

public class TestableNetworkNode extends TileNetworkNode<TestableNetworkNode, TestableNetwork> {
    private final @NotNull @Nonnull Loc4 loc;

    public TestableNetworkNode(@NotNull @Nonnull Loc4 loc) {
        this.loc = loc;
    }

    @Override
    public @NotNull Loc4 getLoc() {
        return loc;
    }
}
