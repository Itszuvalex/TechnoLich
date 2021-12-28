package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class DynamicIBattery implements IBattery {
    private @NotNull @Nonnull
    final Supplier<IBattery> batterySupplier;
    public DynamicIBattery(@NotNull @Nonnull Supplier<IBattery> batterySupplier) {
        this.batterySupplier = batterySupplier;
    }

    @Override
    public double room() {
        return batterySupplier.get().room();
    }

    @Override
    public double fill(double amt) {
        return batterySupplier.get().fill(amt);
    }

    @Override
    public double drain(double amt) {
        return batterySupplier.get().drain(amt);
    }

    @Override
    public double storage() {
        return batterySupplier.get().storage();
    }

    @Override
    public void setStorage(double storage) {
        batterySupplier.get().setStorage(storage);
    }

    @Override
    public double maxStorage() {
        return batterySupplier.get().maxStorage();
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        batterySupplier.get().writeToNbt(tag);
    }

    @Override
    public CompoundTag serializeNBT() {
        return batterySupplier.get().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        batterySupplier.get().deserializeNBT(nbt);
    }
}
