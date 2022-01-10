package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.IModuleProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IModuleCapabilityMap extends ICapabilityProvider, IModuleProvider {
    void invalidateFrags();
    void rehydrateFrags();
}
