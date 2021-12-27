package com.itszuvalex.technolich.api.adapters;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.codehaus.plexus.util.cli.shell.CmdShell;

public interface IBattery extends INBTSerializable<CompoundTag> {
    public static IBattery Empty = new IBattery() {
        @Override
        public double storage() { return 0; }
        @Override
        public void setStorage(double storage) { }
        @Override
        public double maxStorage() { return 0; }
        @Override
        public void writeToNbt(CompoundTag tag) { }
        @Override
        public CompoundTag serializeNBT() { return new CompoundTag(); }
        @Override
        public void deserializeNBT(CompoundTag nbt) { }
    };

    double storage();
    void setStorage(double storage);
    double maxStorage();
    default double room() { return maxStorage() - storage(); }
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

    void writeToNbt(CompoundTag tag);
}
