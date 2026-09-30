package com.itszuvalex.technolich.api.adapters;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public interface IBattery extends ValueIOSerializable {
    IBattery Empty = new IBattery() {
        @Override public double storage() {return 0;}

        @Override public void setStorage(double storage) {}

        @Override public double maxStorage() {return 0;}

        @Override public void serialize(ValueOutput output) {}

        @Override public void deserialize(ValueInput input) {}
    };

    double storage();

    /**
     * Sets the stored energy.  Batteries with a change listener notify it (see {@link #setChanged()}).
     */
    void setStorage(double storage);

    /**
     * Sets the stored energy without notifying any change listener, e.g. inside a NeoForge transaction.
     */
    default void setStorageQuietly(double storage) {
        setStorage(storage);
    }

    double maxStorage();

    default double room() {
        return maxStorage() - storage();
    }

    /**
     * @param amt Energy to add; negative amounts are treated as 0.
     * @return Energy actually added.
     */
    default double fill(double amt) {
        var toFill = Math.max(0, Math.min(amt, room()));
        if (toFill > 0) setStorage(storage() + toFill);
        return toFill;
    }

    /**
     * @param amt Energy to remove; negative amounts are treated as 0.
     * @return Energy actually removed.
     */
    default double drain(double amt) {
        var toDrain = Math.max(0, Math.min(amt, storage()));
        if (toDrain > 0) setStorage(storage() - toDrain);
        return toDrain;
    }

    /**
     * Notifies the battery's change listener, if any (e.g. the owning block entity's {@code setChanged}).
     */
    default void setChanged() {
    }
}
