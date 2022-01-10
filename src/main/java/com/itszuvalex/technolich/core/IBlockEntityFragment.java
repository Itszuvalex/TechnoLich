package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IBlockEntityFragment<T> extends IInternalBlockEntityFragment {
    @NotNull
    @Nonnull
    IModule<T> module();

    @Nonnull
    @NotNull
    Function<Direction, Supplier<LazyOptional<T>>> faceToModuleSupplierMapper(@NotNull @Nonnull IBlockEntity be);
}
