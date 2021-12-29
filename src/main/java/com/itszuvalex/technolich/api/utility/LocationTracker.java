package com.itszuvalex.technolich.api.utility;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LocationTracker {
    private final HashMap<ResourceLocation, HashMap<ChunkCoord, HashSet<Loc4>>> trackerMap;

    public LocationTracker() {
        trackerMap = new HashMap<>();
    }

    public void trackLocation(@Nonnull @NotNull Loc4 loc) {
        FunctionalHelpers.getOrElseUpdate(
                FunctionalHelpers.getOrElseUpdate(trackerMap, loc.dimension().location(), HashMap::new),
                loc.getChunkCoords(),
                HashSet::new
        ).add(loc);
    }

    public void removeLocation(@Nonnull @NotNull Loc4 loc) {
        var locloc = loc.dimension().location();
        if (!trackerMap.containsKey(locloc)) return;
        var dimMap = trackerMap.get(locloc);
        var coords = loc.getChunkCoords();
        if (!dimMap.containsKey(coords)) return;
        var locSet = dimMap.get(coords);
        locSet.remove(loc);
    }

    public boolean isLocationTracked(@Nonnull @NotNull Loc4 loc) {
        var locloc = loc.dimension().location();
        if (!trackerMap.containsKey(locloc)) return false;
        var dimMap = trackerMap.get(locloc);
        var coords = loc.getChunkCoords();
        if (!dimMap.containsKey(coords)) return false;
        var locSet = dimMap.get(coords);
        return locSet.contains(loc);
    }

    public @NotNull
    @Nonnull
    Stream<Loc4> getTrackedLocationsInDim(@Nonnull @NotNull ResourceLocation dim) {
        return FunctionalHelpers.getOptional(trackerMap, dim)
                .map(HashMap::values)
                .map(Collection::stream)
                .map((i) -> i.flatMap(Collection::stream))
                .orElseGet(Stream::empty);
    }

    public @NotNull
    @Nonnull
    Stream<Loc4> getTrackedLocationsInChunk(@Nonnull @NotNull ResourceLocation dim, @Nonnull @NotNull ChunkCoord coords) {
        return FunctionalHelpers.getOptional(trackerMap, dim)
                .flatMap((i) -> FunctionalHelpers.getOptional(i, coords))
                .stream()
                .flatMap(Collection::stream);
    }

    public @NotNull
    @Nonnull
    Stream<Loc4> getAllTrackedLocations() {
        return trackerMap.values().stream()
                .map(HashMap::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream);
    }

    public void clear() {
        trackerMap.clear();
    }

    public void clearDim(@Nonnull @NotNull ResourceLocation dim) {
        FunctionalHelpers.getOptional(trackerMap, dim).ifPresent(HashMap::clear);
    }

    public void clearChunk(@Nonnull @NotNull ResourceLocation dim, @NotNull @Nonnull ChunkCoord coord) {
        FunctionalHelpers.getOptional(trackerMap, dim)
                .flatMap((i) -> FunctionalHelpers.getOptional(i, coord))
                .ifPresent(HashSet::clear);
    }

    private @NotNull
    @Nonnull
    Stream<ChunkCoord> getChunkCoordsInRadius(@NotNull @Nonnull ChunkCoord coords, int blockRadius) {
        return IntStream.rangeClosed(-blockRadius, blockRadius).mapToObj((i) ->
                        IntStream.rangeClosed(-blockRadius, blockRadius).mapToObj((j) ->
                                new ChunkCoord(coords.chunkX() + i, coords.chunkZ() + j)
                        ))
                .flatMap((i) -> i);
    }

    private @NotNull
    @Nonnull
    Stream<ChunkCoord> getChunkCoordsInRadiusInDim(@NotNull @Nonnull ChunkCoord loc,
                                                   int radius,
                                                   @NotNull @Nonnull ResourceLocation dim) {
        return FunctionalHelpers.getOptional(trackerMap, dim)
                .map(HashMap::keySet).stream().flatMap(Collection::stream)
                .filter((floc) ->
                        (floc.chunkX() >= (loc.chunkX() - radius) && floc.chunkX() <= (loc.chunkX() + radius)) &&
                                (floc.chunkZ() >= (loc.chunkZ() - radius) && floc.chunkZ() <= (loc.chunkZ() + radius)));
    }
}
