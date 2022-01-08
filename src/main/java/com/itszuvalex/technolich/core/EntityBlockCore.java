package com.itszuvalex.technolich.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class EntityBlockCore<T extends BlockEntity> extends Block implements EntityBlock {
    protected @NotNull
    @Nonnull
    final Supplier<BlockEntityType<T>> typeSupplier;

    public EntityBlockCore(Properties p_49795_, @NotNull @Nonnull Supplier<BlockEntityType<T>> typeSupplier) {
        super(p_49795_);
        this.typeSupplier = typeSupplier;
    }
}
