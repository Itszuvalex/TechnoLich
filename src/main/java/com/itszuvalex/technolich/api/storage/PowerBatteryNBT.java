package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.nbt.CompoundTag;
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
        return nbt.getDouble(POWER_KEY);
    }

    @Override
    public void setStorage(double storage) {
        nbt.putDouble(POWER_KEY, storage);
    }

    @Override
    public double maxStorage() {
        return nbt.getDouble(POWER_MAX_KEY);
    }

    public void setMaxStorage(double max) {
        nbt.putDouble(POWER_MAX_KEY, max);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putDouble(POWER_KEY, storage());
        tag.putDouble(POWER_MAX_KEY, maxStorage());
    }

    @Override
    public CompoundTag serializeNBT() {
        var ret = new CompoundTag();
        writeToNbt(ret);
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setStorage(nbt.getDouble(POWER_KEY));
        setMaxStorage(nbt.getDouble(POWER_MAX_KEY));
    }
}
