package com.itszuvalex.technolich.api.utility;

public class ModuleCapabilityArrayListMapTest extends ModuleCapabilityMapTestBase {
    @Override
    public IMutableModuleCapabilityMap getMap() {
        return new ModuleCapabilityArrayListMap();
    }
}
