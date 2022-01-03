package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
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
        map.addModule(module, (facing) -> LazyOptional.of(() -> testVal));
        // Assert
        var lazyopt = map.getModule(module, null);
        Assertions.assertTrue(lazyopt.isPresent());
        Assertions.assertTrue(lazyopt.resolve().isPresent());
        Assertions.assertEquals(testVal, lazyopt.resolve().get());
        // Directions
        Arrays.stream(Direction.values()).forEach((dir) ->
        {
            var lazyoptDir = map.getModule(module, dir);
            Assertions.assertTrue(lazyoptDir.isPresent());
            Assertions.assertTrue(lazyoptDir.resolve().isPresent());
            Assertions.assertEquals(testVal, lazyoptDir.resolve().get());
        });
    }

    @Test
    void GetModule_ReturnTheCorrectModuleValueWhenAsked() {
        Integer testVal = 1;
        var map = getMap();
        // Act
        map.addModule(module, (facing) -> facing == null ? LazyOptional.empty() : LazyOptional.of(() -> testVal));
        // Assert
        var lazyopt = map.getModule(module, null);
        Assertions.assertFalse(lazyopt.isPresent());
        // Directions
        Arrays.stream(Direction.values()).forEach((dir) ->
        {
            var lazyoptDir = map.getModule(module, dir);
            Assertions.assertTrue(lazyoptDir.isPresent());
            Assertions.assertTrue(lazyoptDir.resolve().isPresent());
            Assertions.assertEquals(testVal, lazyoptDir.resolve().get());
        });
    }
}
