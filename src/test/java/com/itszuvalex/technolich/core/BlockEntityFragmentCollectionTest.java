package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.TestableLevel;
import com.itszuvalex.technolich.TestableLoc4;
import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.core.frag.BlockEntityFragment;
import com.itszuvalex.technolich.core.frag.InternalBlockEntityFragment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class BlockEntityFragmentCollectionTest {
    static IModule<String> module;

    @BeforeAll
    static void ClassSetup() {
        module = Module.registerModule(Identifier.fromNamespaceAndPath("technolich_test", "fragment_collection"), null);
    }

    @AfterAll
    static void ClassTeardown() {
        Module.clear();
    }

    static BlockEntityFragmentCollection collection() {
        return new BlockEntityFragmentCollection(
                new TestableNetworkNodeBlockEntity(BlockPos.ZERO, new TestableLevel(TestableLoc4.DEFAULT_DIM)));
    }

    static InternalBlockEntityFragment internal(String name) {
        return new InternalBlockEntityFragment() {
            @Override
            public @NotNull String name() {
                return name;
            }
        };
    }

    static BlockEntityFragment<String> exposing(String name, String value) {
        return new BlockEntityFragment<>() {
            @Override
            public @NotNull String name() {
                return name;
            }

            @Override
            public @NotNull IModule<String> module() {
                return module;
            }

            @Override
            public @NotNull Function<Direction, String> faceToModuleMapper(@NotNull IBlockEntity be) {
                return (d) -> value;
            }
        };
    }

    @Test
    void AddInternalFragment_DuplicateName_Throws() {
        var frags = collection();
        frags.addInternalFragment(internal("Inventory"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> frags.addInternalFragment(internal("Inventory")));
    }

    @Test
    void AddFragment_ModuleAlreadyExposed_Throws() {
        var frags = collection();
        frags.addFragment(exposing("First", "a"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> frags.addFragment(exposing("Second", "b")));
        Assertions.assertEquals("a", frags.getModule(module, null).orElseThrow());
    }
}
