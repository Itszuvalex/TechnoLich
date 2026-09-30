package com.itszuvalex.technolich.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers network payloads.  Construct from a {@link RegisterPayloadHandlersEvent} listener on the mod bus,
 * then register payloads with {@link #registrar}.
 */
public class PacketHandler {
    public final PayloadRegistrar registrar;

    public PacketHandler(RegisterPayloadHandlersEvent event, String version) {
        registrar = event.registrar(version);
    }
}
