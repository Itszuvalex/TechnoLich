package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public class SidedSupplier<T> implements ILevelBasedSupplier<T> {
    private final @NotNull
    @Nonnull
    Supplier<T> supplier;
    private final LogicalSide side;

    public SidedSupplier(
            @NotNull
            @Nonnull
                    Supplier<T> supplier, LogicalSide side) {
        this.supplier = supplier;
        this.side = side;
    }

    @Override
    public Optional<T> get(ILevel level) {
        return getSided(level.isClientSide());
    }

    @Override
    public Optional<T> get(Level level) {
        return getSided(level.isClientSide());
    }

    private Optional<T> getSided(boolean clientSide) {
        return (clientSide && (this.side == LogicalSide.CLIENT)) ? Optional.of(supplier.get()) : Optional.empty();
    }
}
