package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.storage.IItemStorage;
import com.itszuvalex.technolich.api.storage.ItemStorageArray;
import com.itszuvalex.technolich.api.utility.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SidedStorageConfigurationTest {
    class TestState {
        IItemStorage invA = new ItemStorageArray(0);
        IItemStorage invB = new ItemStorageArray(0);
        IItemStorage invC = new ItemStorageArray(0);
        String keyA = "A";
        String keyB = "B";
        String keyC = "C";
        Map<String, IItemStorage> map = Map.of(keyA, invA, keyB, invB, keyC, invC);
        Function<Direction, String> defaults = (dir) -> map.keySet().stream().sorted().toList().get(dir.ordinal() % map.size());
        Supplier<Direction> front = () -> Direction.NORTH;
        SidedItemStorageConfiguration config = new SidedItemStorageConfiguration(defaults, map, front);
    }

    TestState getState() {
        return new TestState();
    }

    @Test
    void CycleRelativeFacingStorageBackward_DecrementKeyAndWrapAroundZero() {
        var state = getState();
        var facing = Direction.values()[1];
        Assertions.assertEquals(state.keyB, state.config.getStorageNameForRelativeFacing(facing));
        state.config.cycleRelativeFacingStorageBackward(facing);
        Assertions.assertEquals(state.keyA, state.config.getStorageNameForRelativeFacing(facing));
        state.config.cycleRelativeFacingStorageBackward(facing);
        Assertions.assertEquals(state.keyC, state.config.getStorageNameForRelativeFacing(facing));
        state.config.cycleRelativeFacingStorageBackward(facing);
        Assertions.assertEquals(state.keyB, state.config.getStorageNameForRelativeFacing(facing));
    }

    @Test
    void CycleRelativeFacingStorageForward_IncrementKeyAndWrapAroundEnd() {
        var state = getState();
        var facing = Direction.values()[1];
        Assertions.assertEquals(state.keyB, state.config.getStorageNameForRelativeFacing(facing));
        state.config.cycleRelativeFacingStorageForward(facing);
        Assertions.assertEquals(state.keyC, state.config.getStorageNameForRelativeFacing(facing));
        state.config.cycleRelativeFacingStorageForward(facing);
        Assertions.assertEquals(state.keyA, state.config.getStorageNameForRelativeFacing(facing));
        state.config.cycleRelativeFacingStorageForward(facing);
        Assertions.assertEquals(state.keyB, state.config.getStorageNameForRelativeFacing(facing));
    }

    @Test
    void CycleRelativeFacingIOBackward_DecrementKeyAndWrapAroundZero() {
        var state = getState();
        var facing = Direction.values()[1];
        Assertions.assertEquals(EnumAutomaticIO.NONE, state.config.getIOForRelativeFacing(facing));
        state.config.cycleRelativeFacingIOBackward(facing);
        Assertions.assertEquals(EnumAutomaticIO.OUTPUT, state.config.getIOForRelativeFacing(facing));
        state.config.cycleRelativeFacingIOBackward(facing);
        Assertions.assertEquals(EnumAutomaticIO.INPUT, state.config.getIOForRelativeFacing(facing));
        state.config.cycleRelativeFacingIOBackward(facing);
        Assertions.assertEquals(EnumAutomaticIO.NONE, state.config.getIOForRelativeFacing(facing));
    }

    @Test
    void CycleRelativeFacingIOForward_IncrementKeyAndWrapAroundEnd() {
        var state = getState();
        var facing = Direction.values()[1];
        Assertions.assertEquals(EnumAutomaticIO.NONE, state.config.getIOForRelativeFacing(facing));
        state.config.cycleRelativeFacingIOForward(facing);
        Assertions.assertEquals(EnumAutomaticIO.INPUT, state.config.getIOForRelativeFacing(facing));
        state.config.cycleRelativeFacingIOForward(facing);
        Assertions.assertEquals(EnumAutomaticIO.OUTPUT, state.config.getIOForRelativeFacing(facing));
        state.config.cycleRelativeFacingIOForward(facing);
        Assertions.assertEquals(EnumAutomaticIO.NONE, state.config.getIOForRelativeFacing(facing));
    }

    @Test
    void GetForAbsoluteFacing_RotatedFront_MapsWorldFaceToRelativeFace() {
        var state = getState();
        var config = new SidedItemStorageConfiguration(state.defaults, state.map, () -> Direction.EAST);
        config.cycleRelativeFacingIOForward(Direction.NORTH);
        for (var dir : Direction.values()) {
            var relative = DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(dir, Direction.EAST);
            Assertions.assertEquals(config.getStorageNameForRelativeFacing(relative), config.getStorageNameForAbsoluteFacing(dir));
        }
        // Facing east, the world east face is the front (relative north); up stays up.
        Assertions.assertEquals(config.getStorageNameForRelativeFacing(Direction.NORTH), config.getStorageNameForAbsoluteFacing(Direction.EAST));
        Assertions.assertEquals(config.getStorageNameForRelativeFacing(Direction.UP), config.getStorageNameForAbsoluteFacing(Direction.UP));
        Assertions.assertEquals(EnumAutomaticIO.INPUT, config.getIOForAbsoluteFacing(Direction.EAST));
        Assertions.assertEquals(EnumAutomaticIO.NONE, config.getIOForAbsoluteFacing(Direction.NORTH));
    }

    @Test
    void SerializeDeserialize_RoundTrip() {
        var state = getState();
        state.config.cycleRelativeFacingStorageForward(Direction.UP);
        state.config.cycleRelativeFacingIOBackward(Direction.WEST);
        var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        state.config.serialize(output);
        var loaded = getState().config;
        loaded.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, output.buildResult()));
        for (var dir : Direction.values()) {
            Assertions.assertEquals(state.config.getStorageNameForRelativeFacing(dir), loaded.getStorageNameForRelativeFacing(dir));
            Assertions.assertEquals(state.config.getIOForRelativeFacing(dir), loaded.getIOForRelativeFacing(dir));
        }
    }

    @Test
    void Deserialize_MissingOrUnknownData_FallBackWithoutThrowing() {
        var state = getState();
        var tag = new CompoundTag();
        tag.putString("0", "not a storage");
        tag.putInt("io0", 99);
        tag.putInt("io1", EnumAutomaticIO.INPUT.ordinal()); // Older saves stored ordinals.
        var down = Direction.values()[0];
        var up = Direction.values()[1];
        var expectedDown = state.config.getStorageNameForRelativeFacing(down);
        var expectedUp = state.config.getStorageNameForRelativeFacing(up);

        state.config.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag));

        Assertions.assertEquals(expectedDown, state.config.getStorageNameForRelativeFacing(down));
        Assertions.assertEquals(expectedUp, state.config.getStorageNameForRelativeFacing(up));
        Assertions.assertEquals(EnumAutomaticIO.NONE, state.config.getIOForRelativeFacing(down));
        Assertions.assertEquals(EnumAutomaticIO.INPUT, state.config.getIOForRelativeFacing(up));
        Assertions.assertDoesNotThrow(() -> state.config.cycleRelativeFacingStorageForward(down));
    }

    @Test
    void CycleRelativeFacingStorage_CurrentKeyUnknown_StartFromEnds() {
        var state = getState();
        var config = new SidedItemStorageConfiguration((dir) -> "missing", state.map, state.front);
        config.cycleRelativeFacingStorageForward(Direction.NORTH);
        Assertions.assertEquals(state.keyA, config.getStorageNameForRelativeFacing(Direction.NORTH));
        config.cycleRelativeFacingStorageBackward(Direction.SOUTH);
        Assertions.assertEquals(state.keyC, config.getStorageNameForRelativeFacing(Direction.SOUTH));
    }
}
