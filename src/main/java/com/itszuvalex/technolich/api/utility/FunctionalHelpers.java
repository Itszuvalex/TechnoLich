package com.itszuvalex.technolich.api.utility;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class FunctionalHelpers {
    public static <K, V> V getOrElseUpdate(@NotNull @Nonnull HashMap<K, V> map, @NotNull @Nonnull K key, @NotNull @Nonnull Supplier<V> def) {
        if(map.containsKey(key)) return map.get(key);

        var d = def.get();
        map.put(key, d);
        return d;
    }

    public static <K, V> Optional<V> getOptional(HashMap<K, V> map, K key) {
        if (!map.containsKey(key)) return Optional.empty();
        return Optional.ofNullable(map.get(key));
    }
}
