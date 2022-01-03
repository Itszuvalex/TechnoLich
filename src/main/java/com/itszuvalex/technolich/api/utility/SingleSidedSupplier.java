package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public class SingleSidedSupplier<T> implements ILevelBasedSupplier<T> {
    private final @NotNull
    @Nonnull
    Supplier<T> supplier;
    private final LogicalSide side;

    public SingleSidedSupplier(
            @NotNull
            @Nonnull
                    Supplier<T> supplier, LogicalSide side) {
        this.supplier = supplier;
        this.side = side;
    }

    @Override
    public Optional<T> get(ILevel level) {
        return getSided(SidedHelper.sideFromIsClient(level.isClientSide()));
    }

    @Override
    public Optional<T> get(Level level) {
        return getSided(SidedHelper.sideFromIsClient(level.isClientSide()));
    }

    private Optional<T> getSided(LogicalSide side) {
        return (this.side == side) ? Optional.of(supplier.get()) : Optional.empty();
    }
}
