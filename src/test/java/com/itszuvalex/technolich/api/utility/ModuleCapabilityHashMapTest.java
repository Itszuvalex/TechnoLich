package com.itszuvalex.technolich.api.utility;

public class ModuleCapabilityHashMapTest extends ModuleCapabilityMapTestBase {
    @Override
    public IMutableModuleCapabilityMap getMap() {
        return new ModuleCapabilityHashMap();
    }
}
