package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void serialize(ValueOutput output) {
        batterySupplier.get().serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        batterySupplier.get().deserialize(input);
    }
}
