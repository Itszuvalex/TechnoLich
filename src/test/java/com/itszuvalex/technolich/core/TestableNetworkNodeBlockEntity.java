package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.MCAssert;
import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.IMutableModuleCapabilityMap;
import com.itszuvalex.technolich.api.utility.ModuleCapabilityArrayListMap;
import com.itszuvalex.technolich.api.utility.ModuleCapabilityHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TestableNetworkNodeBlockEntity implements IBlockEntity {
    private final @NotNull
    @Nonnull
    BlockPos pos;
    private final @NotNull
    @Nonnull
    ILevel level;
    public final @NotNull
    @Nonnull
    IMutableModuleCapabilityMap moduleCapabilityMap;

    public TestableNetworkNodeBlockEntity(@NotNull @Nonnull BlockPos pos, @NotNull @Nonnull ILevel level) {
        this.pos = pos;
        this.level = level;
        moduleCapabilityMap = new ModuleCapabilityArrayListMap();
    }

    @Override
    public @NotNull BlockPos getBlockPos() {
        return pos;
    }

    @Override
    public @NotNull BlockEntity toMinecraft() {
        MCAssert.failVanillaClass("toMinecraft");
        return null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return moduleCapabilityMap.getModule(module, side);
    }
}
