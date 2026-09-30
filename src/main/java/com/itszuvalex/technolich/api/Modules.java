package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.api.adapters.IColorable;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import net.minecraft.resources.Identifier;

public class Modules {
    public static final IModule<IColorable> COLORABLE =
            Module.registerModule(Identifier.fromNamespaceAndPath(TechnoLich.NAMELOWER, "colorable"), Capabilities.COLORABLE);

    /**
     * Forces the built-in modules above to register.
     */
    public static void init() {
    }
}
