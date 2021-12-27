package com.itszuvalex.technolich.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    public SimpleChannel CHANNEL;

    public PacketHandler(String name, String type, String version) {
        CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(name, type),
                () -> version,
                version::equals,
                version::equals);
    }
}
