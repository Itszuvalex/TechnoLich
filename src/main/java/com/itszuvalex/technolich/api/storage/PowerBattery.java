package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.nbt.CompoundTag;

public class PowerBattery implements IBattery {
    private static String POWER_KEY = "P";

    private final double maxPower;
    private double curPower;

    public PowerBattery(double powerMax) {
        maxPower = powerMax;
        curPower = 0;
    }

    @Override
    public double storage() {
        return curPower;
    }

    @Override
    public void setStorage(double storage) {
        curPower = storage;
    }

    @Override
    public double maxStorage() {
        return maxPower;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putDouble(POWER_KEY, storage());
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
    }
}
