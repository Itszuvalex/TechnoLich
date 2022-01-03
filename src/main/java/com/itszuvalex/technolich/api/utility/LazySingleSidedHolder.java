package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public class LazySingleSidedHolder<T> implements ILevelBasedSupplier<T> {
    private @NotNull
    @Nonnull
    final
    Lazy<T> obj;
    private final LogicalSide side;

    public LazySingleSidedHolder(@NotNull Supplier<T> factory, LogicalSide side) {
        obj = Lazy.of(factory);
        this.side = side;
    }

    @Override
    public Optional<T> get(ILevel level) {
        return get(SidedHelper.sideFromIsClient(level.isClientSide()));
    }

    @Override
    public Optional<T> get(Level level) {
        return get(SidedHelper.sideFromIsClient(level.isClientSide()));
    }

    public Optional<T> get(LogicalSide side) {
        return (this.side == side) ? Optional.of(obj.get()) : Optional.empty();
    }
}
