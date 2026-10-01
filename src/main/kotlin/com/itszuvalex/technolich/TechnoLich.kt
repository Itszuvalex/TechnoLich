package com.itszuvalex.technolich

import com.itszuvalex.technolich.api.Components
import com.itszuvalex.technolich.api.Modules
import com.itszuvalex.technolich.api.adapters.ILevel
import com.itszuvalex.technolich.api.utility.ChunkCoord
import com.itszuvalex.technolich.core.MultiblockManager
import com.itszuvalex.technolich.core.NetworkManager
import com.itszuvalex.technolich.dev.DevContent
import com.itszuvalex.technolich.team.Research
import com.itszuvalex.technolich.team.TeamDataTypes
import com.itszuvalex.technolich.team.TeamEvents
import com.itszuvalex.technolich.team.TeamManager
import com.mojang.logging.LogUtils
import net.minecraft.world.level.Level
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * TechnoLich mod entry point. Loaded by Kotlin for Forge (`modLoader="kotlinforforge"`), which instantiates this object.
 */
@Mod(TechnoLich.ID)
object TechnoLich {
    const val ID = "technolich"
    const val NAME = "TechnoLich"

    @JvmField
    val LOGGER: Logger = LogUtils.getLogger()

    /**
     * Live server-side block entity networks, ticked from server tick events and cleared when the server stops.
     */
    @JvmField
    val NETWORK_MANAGER = NetworkManager()

    /**
     * Formed multiblock structures on the server, cleared when the server stops. Unlike [NETWORK_MANAGER], it needs no
     * chunk-unload wiring here: each member deregisters itself via
     * [com.itszuvalex.technolich.core.FragMultiblockPart.onChunkUnloaded].
     */
    @JvmField
    val MULTIBLOCK_MANAGER = MultiblockManager()

    /**
     * Teams and their research, loaded when the server starts and saved with the overworld. See [TeamManager].
     */
    @JvmField
    val TEAMS = TeamManager()

    init {
        // Built-in modules must exist before RegisterCapabilitiesEvent
        Modules.init()
        Components.register(MOD_BUS)
        TeamDataTypes.register(Research.TYPE)
        TeamEvents.register(MOD_BUS)

        if (!FMLEnvironment.isProduction()) {
            DevContent.register(MOD_BUS)
        }

        NeoForge.EVENT_BUS.addListener { _: ServerStoppedEvent ->
            NETWORK_MANAGER.clear()
            MULTIBLOCK_MANAGER.clear()
        }
        NeoForge.EVENT_BUS.addListener { _: ServerTickEvent.Pre -> NETWORK_MANAGER.onTickStart() }
        NeoForge.EVENT_BUS.addListener { _: ServerTickEvent.Post -> NETWORK_MANAGER.onTickEnd() }
        NeoForge.EVENT_BUS.addListener(::onChunkUnload)
    }

    /**
     * Networks only track loaded block entities; drop nodes in a chunk as a batch when it unloads.
     */
    private fun onChunkUnload(event: ChunkEvent.Unload) {
        val level = event.level as? Level ?: return
        if (level.isClientSide) return
        val pos = event.chunk.pos
        NETWORK_MANAGER.onChunkUnload(ILevel.of(level), ChunkCoord(pos.x(), pos.z()))
    }
}
