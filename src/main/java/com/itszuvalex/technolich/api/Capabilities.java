package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.util.Color;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class Capabilities {
    public static final BlockCapability<Color, Direction> COLORABLE =
            BlockCapability.createSided(Identifier.fromNamespaceAndPath(TechnoLich.NAMELOWER, "colorable"), Color.class);
}
