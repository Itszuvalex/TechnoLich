package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.Loc4;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public record NetworkEdge(@NotNull @Nonnull Loc4 a, @NotNull @Nonnull Loc4 b) {
}
