package com.itszuvalex.technolich.api.utility;

import com.google.common.math.LongMath;
import com.mojang.math.Vector3f;
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
                FunctionalHelpers.getOrElseUpdate(trackerMap, loc.dimensionId(), HashMap::new),
                loc.getChunkCoords(),
                HashSet::new
        ).add(loc);
    }

    public void removeLocation(@Nonnull @NotNull Loc4 loc) {
        var locloc = loc.dimensionId();
        if (!trackerMap.containsKey(locloc)) return;
        var dimMap = trackerMap.get(locloc);
        var coords = loc.getChunkCoords();
        if (!dimMap.containsKey(coords)) return;
        var locSet = dimMap.get(coords);
        locSet.remove(loc);
    }

    public boolean isLocationTracked(@Nonnull @NotNull Loc4 loc) {
        var locloc = loc.dimensionId();
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

    public @Nonnull
    @NotNull Stream<Loc4> getLocationsInRange(@NotNull @Nonnull Loc4 loc, float range) {
        int radius = (int) Math.ceil(range / MCConstants.CHUNK_SIZE);
        var chunkCoords = ChunkCoord.of(loc.getPos());
        long rsqr;
        try {
            rsqr = LongMath.checkedMultiply(radius, radius);
        } catch (ArithmeticException e) {
            rsqr = -1;
        }

        Stream<ChunkCoord> chunksToCheck;
        // If (radius in chunks) ^2 (total chunks in a 2d square) is greater than the number of chunks tracked,
        // Just go through all tracked chunks instead of generating every chunk and checking HashMap for key existence.
        // Otherwise, say for example we're tracking 100 chunks and we're looking for in a 3x3 grid,
        // Then we should just check for the existence of 9 keys instead of iterating over 100 chunks.
        if ((rsqr > 0) // if radius is too large, don't gen coords to check
                && rsqr < FunctionalHelpers
                .getOptional(trackerMap, loc.dimensionId()).map(HashMap::size).orElse(0)) {
            chunksToCheck = getChunkCoordsInRadius(chunkCoords, radius);
        } else {
            chunksToCheck = getChunkCoordsInRadiusInDim(chunkCoords, radius, loc.dimensionId());
        }

        double rangesqr = range * range;

        return chunksToCheck.flatMap((chunkCoord) ->
                getLocationsInChunk(loc.dimensionId(), chunkCoord)
        ).filter((lo) -> lo.distSqr(loc) <= rangesqr);
    }

    public @NotNull
    @Nonnull
    Stream<Loc4> getLocationsInRange(@NotNull @Nonnull ResourceLocation dim,
                                     Vector3f loc, float range) {
        var chunkCoords = new ChunkCoord(((int) loc.x()) >> 4, ((int) loc.z()) >> 4);
        int radius = (int) Math.ceil(range / MCConstants.CHUNK_SIZE);
        long rsqr;
        try {
            rsqr = LongMath.checkedMultiply(radius, radius);
        } catch (ArithmeticException e) {
            rsqr = -1;
        }

        Stream<ChunkCoord> chunksToCheck;
        // If (radius in chunks) ^2 (total chunks in a 2d square) is greater than the number of chunks tracked,
        // Just go through all tracked chunks instead of generating every chunk and checking HashMap for key existence.
        // Otherwise, say for example we're tracking 100 chunks and we're looking for in a 3x3 grid,
        // Then we should just check for the existence of 9 keys instead of iterating over 100 chunks.
        if ((rsqr > 0) // if radius is too large, don't gen coords to check
                && rsqr < FunctionalHelpers
                .getOptional(trackerMap, dim).map(HashMap::size).orElse(0)) {
            chunksToCheck = getChunkCoordsInRadius(chunkCoords, radius);
        } else {
            chunksToCheck = getChunkCoordsInRadiusInDim(chunkCoords, radius, dim);
        }

        double rangesqr = range * range;

        return chunksToCheck.flatMap((chunkCoord) ->
                getLocationsInChunk(dim, chunkCoord)
        ).filter((lo) ->
                ((lo.x() - loc.x()) * (lo.x() - loc.x()) *
                        (lo.y() - loc.y()) * (lo.y() - loc.y()) *
                        (lo.z() - loc.z()) * (lo.z() - loc.z()))
                        <= rangesqr);
    }

    @Nonnull
    @NotNull
    Stream<Loc4> getLocationsInChunk(@Nonnull @NotNull ResourceLocation dim, @NotNull @Nonnull ChunkCoord chunkLoc) {
        return FunctionalHelpers.getOptional(trackerMap, dim)
                .map((i) -> i.get(chunkLoc))
                .stream()
                .flatMap(Collection::stream);
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
                .filter((floc) -> floc.inRangeOf(loc, radius));
    }
}
