package com.itszuvalex.technolich.api.utility;

import net.minecraftforge.fml.LogicalSide;

public class SidedHelper {
    static public LogicalSide sideFromIsClient(boolean isClient) { return isClient ? LogicalSide.CLIENT : LogicalSide.SERVER; }
}
