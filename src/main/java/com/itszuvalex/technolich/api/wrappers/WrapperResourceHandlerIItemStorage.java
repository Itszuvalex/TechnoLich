package com.itszuvalex.technolich.api.wrappers;

import com.itszuvalex.technolich.api.storage.IItemStorage;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Exposes an {@link IItemStorage} as a NeoForge item {@link ResourceHandler}, e.g. for the
 * {@link net.neoforged.neoforge.capabilities.Capabilities.Item#BLOCK} capability.
 * Transactions are handled by NeoForge's {@link VanillaContainerWrapper}.
 */
public final class WrapperResourceHandlerIItemStorage {
    private WrapperResourceHandlerIItemStorage() {
    }

    public static @NotNull
    @Nonnull
    ResourceHandler<ItemResource> of(@NotNull @Nonnull IItemStorage storage) {
        return VanillaContainerWrapper.of(new WrapperContainerIItemStorage(storage));
    }
}
