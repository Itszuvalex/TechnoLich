package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface IBlockEntityFragment<T> extends IInternalBlockEntityFragment {
    @NotNull
    @Nonnull
    IModule<T> module();

    /**
     * @return Maps a nullable side to the module instance exposed on that side, or null if not exposed there.
     * Called on every query, so it should return the fragment's current instance.
     */
    @Nonnull
    @NotNull
    Function<Direction, T> faceToModuleMapper(@NotNull @Nonnull IBlockEntity be);
}
