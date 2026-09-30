package com.itszuvalex.technolich.api;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.api.adapters.IColorable;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class Capabilities {
    public static final BlockCapability<IColorable, Direction> COLORABLE =
            BlockCapability.createSided(Identifier.fromNamespaceAndPath(TechnoLich.NAMELOWER, "colorable"), IColorable.class);
}
