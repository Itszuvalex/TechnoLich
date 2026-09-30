package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.IModuleProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Resolves modules on an arbitrary BlockEntity through the NeoForge block capability system.
 */
public class WrapperCapabilityProvider implements IModuleProvider {
    private final @NotNull
    @Nonnull
    BlockEntity blockEntity;

    public WrapperCapabilityProvider(@NotNull @Nonnull BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @NotNull
    @Nonnull
    @Override
    public <T> Optional<T> getModule(@NotNull @Nonnull IModule<T> module, @Nullable Direction side) {
        var level = blockEntity.getLevel();
        if (level == null) return Optional.empty();
        return module.blockCapability().map((cap) -> level.getCapability(cap, blockEntity.getBlockPos(),
                blockEntity.getBlockState(), blockEntity, side));
    }
}
