package com.itszuvalex.technolich.network

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

/**
 * Registers network payloads. Construct from a [RegisterPayloadHandlersEvent] listener on the mod bus,
 * then register payloads with [registrar].
 */
class PacketHandler(event: RegisterPayloadHandlersEvent, version: String) {
    @JvmField
    val registrar: PayloadRegistrar = event.registrar(version)
}
