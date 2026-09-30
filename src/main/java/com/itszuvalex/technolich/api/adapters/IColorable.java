package com.itszuvalex.technolich.api.adapters;

import com.itszuvalex.technolich.util.Color;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Something with a color that can be read and changed, e.g. through the
 * {@link com.itszuvalex.technolich.api.Capabilities#COLORABLE} capability.
 */
public interface IColorable {
    @NotNull
    @Nonnull
    Color getColor();

    /**
     * Implementations persist and sync the change as needed.
     */
    void setColor(@NotNull @Nonnull Color color);
}
