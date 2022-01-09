package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public abstract class SidedStorageConfiguration<T> implements INBTSerializable<CompoundTag> {
    private final @NotNull
    @Nonnull
    Map<String, T> storages;
    private final @NotNull
    @Nonnull
    Supplier<Direction> front;
    private final @NotNull
    @Nonnull
    String[] storageSegments;
    private final @NotNull
    @Nonnull
    EnumAutomaticIO[] automaticIO;

    public SidedStorageConfiguration(@NotNull Function<Direction, String> defaults, @NotNull Map<String, T> storages, @NotNull Supplier<Direction> front) {
        this.storages = storages;
        this.front = front;
        storageSegments = new String[Direction.values().length];
        automaticIO = new EnumAutomaticIO[Direction.values().length];
        IntStream.range(0, Direction.values().length).forEach((i) -> {
            storageSegments[i] = defaults.apply(Direction.values()[i]);
            automaticIO[i] = EnumAutomaticIO.NONE;
        });
    }

    public Optional<T> getStorageForGlobalFacing(Direction direction) {
        return Optional.ofNullable(storages.get(getStorageNameForAbsoluteFacing(direction)));
    }

    public Optional<T> getStorageForRelativeFacing(Direction direction) {
        return Optional.ofNullable(storages.get(getStorageNameForRelativeFacing(direction)));
    }

    public void cycleRelativeFacingStorageForward(Direction direction) {
        cycleRelativeFacingStorage(direction, true);
    }

    public void cycleRelativeFacingStorageBackward(Direction direction) {
        cycleRelativeFacingStorage(direction, false);
    }

    public void cycleRelativeFacingIOForward(Direction direction) {
        cycleRelativeFacingIO(direction, true);
    }

    public void cycleRelativeFacingIOBackward(Direction direction) {
        cycleRelativeFacingIO(direction, false);
    }

    public String getStorageNameForRelativeFacing(Direction direction) {
        return storageSegments[direction.ordinal()];
    }

    public String getStorageNameForAbsoluteFacing(Direction direction) {
        return storageSegments[DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(direction, front.get()).ordinal()];
    }

    EnumAutomaticIO getIOForRelativeFacing(Direction direction) {
        return automaticIO[direction.ordinal()];
    }

    EnumAutomaticIO getIOForAbsoluteFacing(Direction direction) {
        return automaticIO[DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(direction, front.get()).ordinal()];
    }

    private void cycleRelativeFacingStorage(Direction dir, boolean forward) {
        var invKey = storageSegments[dir.ordinal()];
        List<String> keys = storages.keySet().stream().sorted().toList();
        var invInd = IntStream.range(0, keys.size()).filter((i) -> keys.get(i).equals(invKey)).findFirst().getAsInt();
        var shift = forward ? 1 : -1;
        storageSegments[dir.ordinal()] = keys.get(((invInd + shift) + keys.size()) % keys.size());
    }

    private void cycleRelativeFacingIO(Direction dir, boolean forward) {
        var invEnum = automaticIO[dir.ordinal()];
        var keys = EnumAutomaticIO.values();
        var invInd = invEnum.ordinal();
        var shift = forward ? 1 : -1;
        automaticIO[dir.ordinal()] = keys[((invInd + shift) + keys.length) % keys.length];
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        IntStream.range(0, Direction.values().length).forEach((i) -> {
            tag.putString(String.valueOf(i), storageSegments[i]);
            tag.putInt("io" + i, automaticIO[i].ordinal());
        });
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        IntStream.range(0, Direction.values().length).forEach((i) -> {
            storageSegments[i] = nbt.getString(String.valueOf(i));
            automaticIO[i] = EnumAutomaticIO.values()[nbt.getInt("io" + i)];
        });
    }
}
