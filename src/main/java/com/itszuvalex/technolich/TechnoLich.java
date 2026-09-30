package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.Modules;
import com.itszuvalex.technolich.api.utility.LazySingleSidedHolder;
import com.itszuvalex.technolich.core.NetworkManager;
import com.itszuvalex.technolich.dev.DevContent;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(TechnoLich.NAMELOWER)
public class TechnoLich {
    public static final String NAME = "TechnoLich";
    public static final String NAMELOWER = "technolich";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final LazySingleSidedHolder<NetworkManager> NETWORK_MANAGER = new LazySingleSidedHolder<>(NetworkManager::new, LogicalSide.SERVER);

    public TechnoLich(IEventBus modEventBus, ModContainer modContainer) {
        // Built-in modules must exist before RegisterCapabilitiesEvent
        Modules.init();

        if (!FMLEnvironment.isProduction()) {
            DevContent.register(modEventBus);
        }

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        NETWORK_MANAGER.get(LogicalSide.SERVER).ifPresent(NetworkManager::clear);
    }

    @SubscribeEvent
    public void onServerTickPre(ServerTickEvent.Pre event) {
        NETWORK_MANAGER.get(LogicalSide.SERVER).ifPresent(NetworkManager::onTickStart);
    }

    @SubscribeEvent
    public void onServerTickPost(ServerTickEvent.Post event) {
        NETWORK_MANAGER.get(LogicalSide.SERVER).ifPresent(NetworkManager::onTickEnd);
    }
}
