package com.itszuvalex.technolich.api.utility;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Overideable<T> {
    private T defaultValue;
    private Optional<T> overrideValue;

    public Overideable(@NotNull @Nonnull T defaultValue) {
        this.defaultValue = defaultValue;
        this.overrideValue = Optional.empty();
    }

    public @NotNull
    @Nonnull
    T get() {
        return overrideValue.orElseGet(() -> defaultValue);
    }

    public void revert() {overrideValue = Optional.empty();}

    public void setOverrideValue(@NotNull @Nonnull T overrideValue) {this.overrideValue = Optional.of(overrideValue);}

    public boolean isOverriden() {return overrideValue.isPresent();}
}
