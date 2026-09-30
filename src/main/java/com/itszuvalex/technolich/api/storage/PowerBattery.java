package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class PowerBattery implements IBattery {
    private static final String POWER_KEY = "P";

    private final double maxPower;
    private double curPower;
    private final @NotNull
    @Nonnull
    Runnable onChanged;

    /**
     * @param onChanged Run after every {@link #setStorage} and {@link #setChanged()}, e.g. the owning block entity's
     *                  {@code setChanged}.
     */
    public PowerBattery(double powerMax, @NotNull @Nonnull Runnable onChanged) {
        maxPower = powerMax;
        curPower = 0;
        this.onChanged = onChanged;
    }

    public PowerBattery(double powerMax) {
        this(powerMax, () -> {});
    }

    @Override
    public double storage() {
        return curPower;
    }

    @Override
    public void setStorage(double storage) {
        curPower = storage;
        onChanged.run();
    }

    @Override
    public void setStorageQuietly(double storage) {
        curPower = storage;
    }

    @Override
    public double maxStorage() {
        return maxPower;
    }

    @Override
    public void setChanged() {
        onChanged.run();
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putDouble(POWER_KEY, storage());
    }

    @Override
    public void deserialize(ValueInput input) {
        setStorage(input.getDoubleOr(POWER_KEY, 0));
    }
}
