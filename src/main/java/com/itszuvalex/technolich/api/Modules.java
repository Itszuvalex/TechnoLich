package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.util.Color;
import net.minecraft.resources.Identifier;

public class Modules {
    public static final IModule<Color> COLORABLE =
            Module.registerModule(Identifier.fromNamespaceAndPath(TechnoLich.NAMELOWER, "colorable"), Capabilities.COLORABLE);

    /**
     * Forces the built-in modules above to register.
     */
    public static void init() {
    }
}
