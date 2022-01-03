package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class WrapperBlockEntity implements IBlockEntity {
    private final @Nonnull
    BlockEntity entity;
    private final @Nonnull
    WrapperCapabilityProvider capabilityProvider;

    public WrapperBlockEntity(@NotNull @Nonnull BlockEntity entity) {
        this.entity = entity;
        this.capabilityProvider = new WrapperCapabilityProvider(entity);
    }

    public @NotNull
    @Nonnull
    @Override
    BlockPos getBlockPos() {return entity.getBlockPos();}

    @Override
    public @NotNull BlockEntity toMinecraft() {
        return entity;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return capabilityProvider.getModule(module, side);
    }
}
