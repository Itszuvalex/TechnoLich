package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class BlockEntityFragmentCollection implements IBlockEntityEventHandler, IBlockEntityBlockEventHandler, IScopedSerialization, IModuleCapabilityMap, IBlockEntityTickable {
    private final @NotNull
    @Nonnull
    IMutableModuleCapabilityMap modCapMap;
    private final @NotNull
    @Nonnull
    ArrayList<IInternalBlockEntityFragment> modList;
    private final @NotNull
    @Nonnull
    ArrayList<IBlockEntityTickable> tickList;
    private final @NotNull
    @Nonnull
    IFragmentHost host;
    private final @NotNull
    @Nonnull
    Set<String> fragmentNames = new HashSet<>();
    private final @NotNull
    @Nonnull
    Set<IModule<?>> exposedModules = new HashSet<>();

    public BlockEntityFragmentCollection(@NotNull @Nonnull IFragmentHost host) {
        modCapMap = new ModuleCapabilityArrayListMap();
        modList = new ArrayList<>();
        tickList = new ArrayList<>();
        this.host = host;
    }

    /**
     * @throws IllegalArgumentException if a fragment with the same {@link IInternalBlockEntityFragment#name()} was
     *                                  already added; names key each fragment's saved data, so duplicates would
     *                                  overwrite each other.
     */
    public void addInternalFragment(@NotNull @Nonnull IInternalBlockEntityFragment fragment) {
        if (!fragmentNames.add(fragment.name()))
            throw new IllegalArgumentException("Duplicate fragment name: " + fragment.name());
        modList.add(fragment);
        fragment.onAttach(host);
    }

    /**
     * @throws IllegalArgumentException if the name is a duplicate, or another fragment already exposes this module.
     */
    public <F> void addFragment(@NotNull @Nonnull IBlockEntityFragment<F> fragment) {
        if (exposedModules.contains(fragment.module()))
            throw new IllegalArgumentException("Module " + fragment.module().id() + " is already exposed by another fragment");
        addInternalFragment(fragment);
        exposedModules.add(fragment.module());
        var getter = fragment.faceToModuleMapper(host.blockEntity());
        modCapMap.addModule(fragment.module(), getter);
    }

    /**
     * Exposes a capability that is not tied to a module, e.g. one of {@link com.itszuvalex.technolich.api.ModuleCapabilities#STANDARD}.
     */
    public <T> void addCapability(@NotNull @Nonnull BlockCapability<T, Direction> cap, @NotNull @Nonnull Function<Direction, T> provider) {
        modCapMap.addCapability(cap, provider);
    }

    public void addTickable(@NotNull @Nonnull IBlockEntityTickable tickable) {
        tickList.add(tickable);
    }

    @Override
    public void tick(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState state) {
        tickList.forEach((t) -> t.tick(level, pos, state));
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull ValueOutput output) {
        modList.stream()
                .filter((i) -> i.handlesScope(scope))
                .forEach((i) -> i.serializeTo(scope, output.child(i.name())));
    }

    @Override
    public void deserialize(@NotNull ValueInput input, NBTSerializationScope scope) {
        modList.stream()
                .filter((i) -> i.handlesScope(scope))
                .forEach((i) -> input.child(i.name()).ifPresent((child) -> i.deserialize(child, scope)));
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return modList.stream().anyMatch((i) -> i.handlesScope(scope));
    }

    @Override
    public @NotNull <T> Optional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return modCapMap.getModule(module, side);
    }

    @Override
    public <T> @Nullable T getCapability(@NotNull BlockCapability<T, Direction> cap, @Nullable Direction side) {
        return modCapMap.getCapability(cap, side);
    }

    @Override
    public void invalidateFrags() {
        modCapMap.invalidateFrags();
        modList.forEach(IBlockEntityEventHandler::invalidateFrags);
    }

    @Override
    public void rehydrateFrags() {
        modList.forEach(IBlockEntityEventHandler::rehydrateFrags);
        modCapMap.rehydrateFrags();
    }

    @Override
    public void onRemove(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState blockStatePrev) {
        modList.forEach((i) -> i.onRemove(level, pos, blockStatePrev));
    }
}
