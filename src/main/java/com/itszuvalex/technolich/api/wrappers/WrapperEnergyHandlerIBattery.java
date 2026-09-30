package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.adapters.IBattery;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Exposes an {@link IBattery} as a NeoForge {@link EnergyHandler}, e.g. for the
 * {@link net.neoforged.neoforge.capabilities.Capabilities.Energy#BLOCK} capability.
 * Energy is converted 1:1 and truncated to whole units.
 */
public class WrapperEnergyHandlerIBattery extends SnapshotJournal<Double> implements EnergyHandler {
    private final @NotNull
    @Nonnull
    IBattery battery;

    public WrapperEnergyHandlerIBattery(@NotNull @Nonnull IBattery battery) {
        this.battery = battery;
    }

    @Override
    public long getAmountAsLong() {
        return (long) Math.floor(battery.storage());
    }

    @Override
    public long getCapacityAsLong() {
        return (long) Math.floor(battery.maxStorage());
    }

    @Override
    public int insert(int amount, @NotNull TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        var toFill = (int) Math.min(amount, Math.floor(battery.room()));
        if (toFill <= 0) return 0;
        updateSnapshots(transaction);
        // Quiet while the transaction is open; the battery is notified once, on root commit.
        battery.setStorageQuietly(battery.storage() + toFill);
        return toFill;
    }

    @Override
    public int extract(int amount, @NotNull TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        var toDrain = (int) Math.min(amount, Math.floor(battery.storage()));
        if (toDrain <= 0) return 0;
        updateSnapshots(transaction);
        battery.setStorageQuietly(battery.storage() - toDrain);
        return toDrain;
    }

    @Override
    protected Double createSnapshot() {
        return battery.storage();
    }

    @Override
    protected void revertToSnapshot(Double snapshot) {
        battery.setStorageQuietly(snapshot);
    }

    @Override
    protected void onRootCommit(Double originalState) {
        battery.setChanged();
    }
}
