package com.itszuvalex.technolich.api.wrappers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Uses System.identity to measure equality of keys. This way we're guaranteed one wrapper per in-memory object.
 * This should protect us from Client/Server sidedness.
 *
 * Not expected to see much use - wrappers are paper-thin
 * However, even though premature optimization is the root of all evil
 * I can see it being very helpful to cache widely-used but mostly unchanging things
 * Such as Levels.  Rather than constantly having to wrap the same level over and over,
 * just cache it.
 *
 * Players/Entities might also benefit from this, should I ever want to wrap their impls.
 *
 * Not necessarily suitable for ItemStacks or other wide-spread data structures.
 *
 * @param <C> Core class to be wrapped
 * @param <W> Wrapper class
 */
public class WrapperCache<C, W> {
    private final @NotNull
    @Nonnull
    LoadingCache<C, W> implCache;

    /**
     *
     *
     * @param cap Capacity of objects
     * @param loader Function to produce a new object should it not exist in cache.
     */
    public WrapperCache(int cap, @NotNull Function<C, W> loader) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.initialCapacity(cap).maximumSize(cap).concurrencyLevel(1).weakKeys();
        implCache = cacheBuilder.build(new CacheLoader<>() {
            @Override
            public @NotNull W load(@NotNull C key) throws Exception {
                return loader.apply(key);
            }
        });
    }

    public W get(@NotNull C core) {
        return implCache.getUnchecked(core);
    }
}
