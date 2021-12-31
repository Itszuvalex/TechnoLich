package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class WrapperLevel implements ILevel {
    private @Nonnull final Level level;
    public WrapperLevel(@NotNull @Nonnull Level level) {
        this.level = level;
    }

    @Override
    public boolean isClientSide() {
        return level.isClientSide();
    }

    @Override
    public @NotNull Level toMinecraft() {
        return level;
    }
}
