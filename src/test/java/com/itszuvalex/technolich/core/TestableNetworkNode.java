package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.Loc4;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.annotation.Testable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class TestableNetworkNode extends TileNetworkNode<TestableNetworkNode, TestableNetwork> {
    private final @NotNull @Nonnull Loc4 loc;
    /**
     * Locations this node has been told it is connected to, via onConnect/onDisconnect.
     */
    public final @NotNull @Nonnull Set<Loc4> connectedTo = new HashSet<>();

    public TestableNetworkNode(@NotNull @Nonnull Loc4 loc) {
        this.loc = loc;
    }

    @Override
    public @NotNull Loc4 getLoc() {
        return loc;
    }

    @Override
    public void onConnect(@NotNull Loc4 loc) {
        connectedTo.add(loc);
    }

    @Override
    public void onDisconnect(@NotNull Loc4 loc) {
        connectedTo.remove(loc);
    }
}
