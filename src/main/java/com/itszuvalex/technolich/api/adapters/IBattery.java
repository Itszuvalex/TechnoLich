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

    void setStorage(double storage);

    double maxStorage();

    default double room() {
        return maxStorage() - storage();
    }

    default double fill(double amt) {
        var toFill = Math.min(amt, room());
        setStorage(storage() + toFill);
        return toFill;
    }

    default double drain(double amt) {
        var toDrain = Math.min(amt, storage());
        setStorage(storage() - toDrain);
        return toDrain;
    }
}
