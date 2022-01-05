package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class BlockEntityFragmentCollection implements IBlockEntityEventHandler, IScopedNBTSerialization<CompoundTag>, IModuleCapabilityMap {
    private final @NotNull
    @Nonnull
    IMutableModuleCapabilityMap modCapMap;
    private final @NotNull
    @Nonnull
    ArrayList<IInternalBlockEntityFragment> modList;
    private final @NotNull
    @Nonnull
    IBlockEntity blockEntity;

    public BlockEntityFragmentCollection(@NotNull @Nonnull IBlockEntity blockEntity) {
        modCapMap = new ModuleCapabilityArrayListMap();
        modList = new ArrayList<>();
        this.blockEntity = blockEntity;
    }

    public void addInternalFragment(@NotNull @Nonnull IInternalBlockEntityFragment fragment) {
        modList.add(fragment);
    }

    public void addFragment(@NotNull @Nonnull IBlockEntityFragment<?> fragment) {
        addInternalFragment(fragment);
        var getter = fragment.faceToModuleMapper(blockEntity);
        modCapMap.addModule(fragment.module(), getter::apply);
    }

    @Override
    public @NotNull CompoundTag serialize(NBTSerializationScope scope) {
        var tag = new CompoundTag();
        modList.stream()
                .filter((i) -> i.handlesScope(scope))
                .forEach((i) -> tag.put(i.name(), i.serialize(scope)));
        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {
        modList.stream()
                .filter((i) -> i.handlesScope(scope) && nbt.contains(i.name()))
                .forEach((i) -> i.deserialize(nbt.getCompound(i.name()), scope));
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return true;
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
}
