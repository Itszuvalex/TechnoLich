package com.itszuvalex.technolich.api.adapters;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleTest {
    @AfterEach
    void MethodTeardown() {
        Module.clear();
    }

    @Test
    void RegisterModule_DuplicateId_Throws() {
        var id = Identifier.fromNamespaceAndPath("technolich_test", "dup");
        Module.registerModule(id, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> Module.registerModule(id, null));
    }

    @Test
    void RegisterModule_SamePathDifferentNamespace_BothRegister() {
        var a = Module.registerModule(Identifier.fromNamespaceAndPath("mod_a", "colorable"), null);
        var b = Module.registerModule(Identifier.fromNamespaceAndPath("mod_b", "colorable"), null);
        Assertions.assertNotEquals(a.id(), b.id());
        Assertions.assertTrue(Module.modules().contains(a));
        Assertions.assertTrue(Module.modules().contains(b));
    }
}
