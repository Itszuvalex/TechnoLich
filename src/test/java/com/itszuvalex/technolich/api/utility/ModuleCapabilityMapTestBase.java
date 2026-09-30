package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public abstract class ModuleCapabilityMapTestBase {
    static IModule<Integer> module;

    @BeforeAll
    public static void ClassSetup() {
        module = Module.registerModule("TestModule", null);
    }

    @AfterAll
    public static void ClassTeardown() {
        Module.clear();
    }

    public abstract IMutableModuleCapabilityMap getMap();

    @Test
    void AddModule_RegisterAndHaveModule() {
        Integer testVal = 1;
        var map = getMap();
        // Act
        map.addModule(module, (facing) -> testVal);
        // Assert
        var opt = map.getModule(module, null);
        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals(testVal, opt.get());
        // Directions
        Arrays.stream(Direction.values()).forEach((dir) ->
        {
            var optDir = map.getModule(module, dir);
            Assertions.assertTrue(optDir.isPresent());
            Assertions.assertEquals(testVal, optDir.get());
        });
    }

    @Test
    void GetModule_ReturnTheCorrectModuleValueWhenAsked() {
        Integer testVal = 1;
        var map = getMap();
        // Act
        map.addModule(module, (facing) -> facing == null ? null : testVal);
        // Assert
        var opt = map.getModule(module, null);
        Assertions.assertFalse(opt.isPresent());
        // Directions
        Arrays.stream(Direction.values()).forEach((dir) ->
        {
            var optDir = map.getModule(module, dir);
            Assertions.assertTrue(optDir.isPresent());
            Assertions.assertEquals(testVal, optDir.get());
        });
    }
}
