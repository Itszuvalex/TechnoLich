package com.itszuvalex.technolich.team

import com.itszuvalex.technolich.TechnoLich
import com.itszuvalex.technolich.network.PacketHandler
import com.mojang.logging.LogUtils
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.server.ServerLifecycleHooks

/**
 * Server to client: the receiving player's own team, encoded as [TeamCodec] saves it.
 */
data class TeamSyncPayload(val team: CompoundTag) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<TeamSyncPayload> = TYPE

    companion object {
        @JvmField
        val TYPE: CustomPacketPayload.Type<TeamSyncPayload> =
            CustomPacketPayload.Type(Identifier.fromNamespaceAndPath(TechnoLich.ID, "team_sync"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TeamSyncPayload> =
            ByteBufCodecs.COMPOUND_TAG.map(::TeamSyncPayload, TeamSyncPayload::team).cast()

        /**
         * Client side. A payload that does not decode is logged and ignored; the previous copy stays.
         */
        @JvmStatic
        fun handle(payload: TeamSyncPayload, context: IPayloadContext) {
            try {
                ClientTeam.current = TeamCodec.decodeTeam(payload.team, context.player().registryAccess().createSerializationContext(NbtOps.INSTANCE))
            } catch (e: Exception) {
                LOGGER.error("Ignoring a team sync that could not be read", e)
            }
        }

        private val LOGGER = LogUtils.getLogger()
    }
}

/**
 * The client's read-only copy of its own team, replaced whenever the server sends a [TeamSyncPayload] (on login and
 * after every change to the team). Null before the first sync.
 */
object ClientTeam {
    @Volatile
    var current: Team? = null
        internal set
}

/**
 * Wires [TechnoLich.TEAMS] into the server lifecycle: load when the server starts, save with the overworld (autosave
 * and shutdown), give each joining player a solo team, sync each player's team, and register `/technolich team`.
 */
object TeamEvents {
    const val NETWORK_VERSION = "1"

    @JvmStatic
    fun register(modBus: IEventBus) {
        modBus.addListener(::registerPayloads)
        NeoForge.EVENT_BUS.addListener(::onServerStarting)
        NeoForge.EVENT_BUS.addListener(::onLevelSave)
        NeoForge.EVENT_BUS.addListener(::onServerStopped)
        NeoForge.EVENT_BUS.addListener(::onLogin)
        NeoForge.EVENT_BUS.addListener { event: RegisterCommandsEvent -> TeamCommands.register(event.dispatcher) }
        TechnoLich.TEAMS.onChange(::syncChanged)
    }

    private fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        PacketHandler(event, NETWORK_VERSION).registrar
            .playToClient(TeamSyncPayload.TYPE, TeamSyncPayload.STREAM_CODEC, TeamSyncPayload::handle)
    }

    private fun onServerStarting(event: ServerStartingEvent) {
        val server = event.server
        val file = server.getWorldPath(LevelResource.DATA).resolve(TechnoLich.ID).resolve("teams.dat")
        TechnoLich.TEAMS.load(TeamStore(file), server.registryAccess().createSerializationContext(NbtOps.INSTANCE))
    }

    private fun onLevelSave(event: LevelEvent.Save) {
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() == Level.OVERWORLD) TechnoLich.TEAMS.save()
    }

    private fun onServerStopped(event: ServerStoppedEvent) {
        TechnoLich.TEAMS.save()
        TechnoLich.TEAMS.unload()
    }

    private fun onLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        TechnoLich.TEAMS.change { it.ensurePlayer(player.uuid, player.name.string) }
        send(player)
    }

    private fun syncChanged(old: TeamState, new: TeamState) {
        val server = ServerLifecycleHooks.getCurrentServer() ?: return
        for (player in server.playerList.players) {
            if (old.teamOf(player.uuid) != new.teamOf(player.uuid)) send(player)
        }
    }

    private fun send(player: ServerPlayer) {
        val team = TechnoLich.TEAMS.state.teamOf(player.uuid) ?: return
        val ops = player.registryAccess().createSerializationContext(NbtOps.INSTANCE)
        PacketDistributor.sendToPlayer(player, TeamSyncPayload(TeamCodec.encodeTeam(team, ops)))
    }
}
