package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.ScopedCompoundTagSerialization;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IInternalBlockEntityFragment extends IBlockEntityEventHandler, ScopedCompoundTagSerialization {
    @NotNull
    @Nonnull
    String name();

}
