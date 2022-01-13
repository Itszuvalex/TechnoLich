package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class BlockEntityFragmentCollection implements IBlockEntityEventHandler, IBlockEntityBlockEventHandler, ScopedCompoundTagSerialization, IModuleCapabilityMap, IBlockEntityTickable {
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
    IBlockEntity blockEntity;

    public BlockEntityFragmentCollection(@NotNull @Nonnull IBlockEntity blockEntity) {
        modCapMap = new ModuleCapabilityArrayListMap();
        modList = new ArrayList<>();
        tickList = new ArrayList<>();
        this.blockEntity = blockEntity;
    }

    public void addInternalFragment(@NotNull @Nonnull IInternalBlockEntityFragment fragment) {
        modList.add(fragment);
    }

    public <F> void addFragment(@NotNull @Nonnull IBlockEntityFragment<F> fragment) {
        addInternalFragment(fragment);
        var getter = fragment.faceToModuleSupplierMapper(blockEntity);
        modCapMap.addModule(fragment.module(), getter);
    }

    public void addTickable(@NotNull @Nonnull IBlockEntityTickable tickable) {
        tickList.add(tickable);
    }

    @Override
    public void tick(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState state) {
        tickList.forEach((t) -> t.tick(level, pos, state));
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull CompoundTag tag) {
        modList.stream()
                .filter((i) -> i.handlesScope(scope))
                .forEach((i) -> tag.put(i.name(), i.serialize(scope)));
    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {
        modList.stream()
                .filter((i) -> i.handlesScope(scope) && nbt.contains(i.name()))
                .forEach((i) -> i.deserialize(nbt.getCompound(i.name()), scope));
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return modList.stream().anyMatch((i) -> i.handlesScope(scope));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return modCapMap.getModule(module, side);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
