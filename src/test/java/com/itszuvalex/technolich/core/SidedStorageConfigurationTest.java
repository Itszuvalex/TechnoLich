package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.storage.IItemStorage;
import com.itszuvalex.technolich.api.storage.ItemStorageArray;
import net.minecraft.core.Direction;
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
}
