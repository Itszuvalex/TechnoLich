package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.core.frag.BlockEntityFragment;
import com.itszuvalex.technolich.core.frag.InternalBlockEntityFragment;
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
        return new BlockEntityFragmentCollection(new TestableFragmentHost());
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
    void AddInternalFragment_AttachesHost() {
        var host = new TestableFragmentHost();
        var frags = new BlockEntityFragmentCollection(host);
        var frag = new InternalBlockEntityFragment() {
            @Override
            public @NotNull String name() {
                return "Dirtying";
            }

            void change() {
                markDirty();
                markDirtyAndSync();
            }
        };
        frag.change(); // Not attached yet: no-op.
        Assertions.assertEquals(0, host.dirtyCount);

        frags.addInternalFragment(frag);
        frag.change();

        Assertions.assertEquals(2, host.dirtyCount);
        Assertions.assertEquals(1, host.syncCount);
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
