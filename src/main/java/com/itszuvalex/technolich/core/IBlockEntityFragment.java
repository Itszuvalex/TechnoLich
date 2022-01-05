package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.Module;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface IBlockEntityFragment<T> extends IInternalBlockEntityFragment {
    @NotNull
    @Nonnull
    Module<T> module();

    @Nonnull
    @NotNull
    Function<Direction, LazyOptional<T>> faceToModuleMapper(@NotNull @Nonnull IBlockEntity be);
}
