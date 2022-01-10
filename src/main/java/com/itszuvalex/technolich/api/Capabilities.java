package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.util.Color;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Capabilities {
    static Capability<Color> COLORABLE = CapabilityManager.get(new CapabilityToken<>(){});
}
