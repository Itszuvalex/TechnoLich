package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Utility value to gate in-memory data against a {@link ILevel} or {@link Level}.
 * This masks how the data is stored - for example, it could be
 *      * Capabilities
 *      * In-Memory Map
 *      * Sided memory that uses {@link Level#isClientSide()} to return the appropriate storage.
 *
 * @param <T> Data holder
 */
public interface ILevelBasedSupplier<T> {
    Optional<T> get(ILevel level);

    Optional<T> get(Level level);
}
