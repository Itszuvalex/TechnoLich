package com.itszuvalex.technolich.api.adapters;

import net.minecraftforge.common.capabilities.Capability;

import java.util.Optional;

public interface IModule<T> {
    Optional<Capability<T>> capability();
    String name();
}
