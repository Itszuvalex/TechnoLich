package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.adapters.Module;
import com.itszuvalex.technolich.util.Color;

public class Modules {
    public static IModule<Color> COLORABLE = Module.registerModule("Colorable", () -> Capabilities.COLORABLE);
}
