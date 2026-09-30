package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.utility.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public abstract class SidedStorageConfiguration<T> implements ValueIOSerializable {
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
    private final @NotNull
    @Nonnull
    Function<Direction, String> defaults;

    public SidedStorageConfiguration(@NotNull Function<Direction, String> defaults, @NotNull Map<String, T> storages, @NotNull Supplier<Direction> front) {
        this.defaults = defaults;
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
        if (keys.isEmpty()) return;
        var invInd = keys.indexOf(invKey);
        // Unknown key: forward lands on the first key, backward on the last.
        if (invInd < 0) invInd = forward ? -1 : 0;
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
    public void serialize(ValueOutput output) {
        IntStream.range(0, Direction.values().length).forEach((i) -> {
            output.putString(String.valueOf(i), storageSegments[i]);
            output.putString("io" + i, automaticIO[i].name());
        });
    }

    /**
     * Missing or unknown values fall back to the defaults rather than failing, so a storage that was renamed or
     * removed, or corrupt data, can't crash loading.
     */
    @Override
    public void deserialize(ValueInput input) {
        IntStream.range(0, Direction.values().length).forEach((i) -> {
            storageSegments[i] = input.getString(String.valueOf(i))
                    .filter(storages::containsKey)
                    .orElseGet(() -> defaults.apply(Direction.values()[i]));
            automaticIO[i] = readIO(input, "io" + i);
        });
    }

    private static @NotNull
    @Nonnull
    EnumAutomaticIO readIO(@NotNull @Nonnull ValueInput input, @NotNull @Nonnull String key) {
        var values = EnumAutomaticIO.values();
        return input.getString(key)
                .flatMap((name) -> Arrays.stream(values).filter((v) -> v.name().equals(name)).findFirst())
                // Older saves stored the ordinal.
                .or(() -> input.getInt(key).filter((o) -> o >= 0 && o < values.length).map((o) -> values[o]))
                .orElse(EnumAutomaticIO.NONE);
    }
}
