package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class PowerBatteryNBT implements IBattery {
    private static String POWER_KEY = "P";
    private static String POWER_MAX_KEY = "M";

    private final @NotNull CompoundTag nbt;

    public PowerBatteryNBT(@NotNull @Nonnull CompoundTag nbt) {
        this.nbt = nbt;
    }

    public PowerBatteryNBT(@NotNull @Nonnull CompoundTag nbt, double max) {
        this(nbt);
        setMaxStorage(max);
    }

    @Override
    public double storage() {
        return nbt.getDoubleOr(POWER_KEY, 0);
    }

    @Override
    public void setStorage(double storage) {
        nbt.putDouble(POWER_KEY, storage);
    }

    @Override
    public double maxStorage() {
        return nbt.getDoubleOr(POWER_MAX_KEY, 0);
    }

    public void setMaxStorage(double max) {
        nbt.putDouble(POWER_MAX_KEY, max);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putDouble(POWER_KEY, storage());
        output.putDouble(POWER_MAX_KEY, maxStorage());
    }

    @Override
    public void deserialize(ValueInput input) {
        setStorage(input.getDoubleOr(POWER_KEY, 0));
        setMaxStorage(input.getDoubleOr(POWER_MAX_KEY, 0));
    }
}
