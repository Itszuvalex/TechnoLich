package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Battery backed by a NeoForge {@link EnergyHandler}, e.g. another mod's energy capability.
 * Energy is converted 1:1 and truncated to whole units.
 * Mutations open root transactions, so they must not be called while a transaction is open.
 */
public class BatteryEnergyHandler implements IBattery {
    private @NotNull @Nonnull
    final EnergyHandler handler;

    public BatteryEnergyHandler(@NotNull @Nonnull EnergyHandler handler) {
        this.handler = handler;
    }

    @Override
    public double storage() {
        return handler.getAmountAsLong();
    }

    @Override
    public void setStorage(double storage) {
        var diff = storage - storage();
        if (diff > 0) fill(diff);
        else if (diff < 0) drain(-diff);
    }

    @Override
    public double maxStorage() {
        return handler.getCapacityAsLong();
    }

    @Override
    public double fill(double amt) {
        try (var tx = Transaction.openRoot()) {
            var filled = handler.insert(toInt(amt), tx);
            tx.commit();
            return filled;
        }
    }

    @Override
    public double drain(double amt) {
        try (var tx = Transaction.openRoot()) {
            var drained = handler.extract(toInt(amt), tx);
            tx.commit();
            return drained;
        }
    }

    /**
     * The backing handler owns its persistence.
     */
    @Override
    public void serialize(ValueOutput output) {
    }

    @Override
    public void deserialize(ValueInput input) {
    }

    private static int toInt(double amt) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, Math.floor(amt)));
    }
}
