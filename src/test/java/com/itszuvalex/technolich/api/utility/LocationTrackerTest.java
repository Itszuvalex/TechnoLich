package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.TestableLoc4;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

class LocationTrackerTest {

    @Test
    void TrackLocation_ShouldTrackLocation() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        // Act
        tracker.trackLocation(loc);
        // Assert
        Assertions.assertTrue(tracker.isLocationTracked(loc));
    }

    @Test
    void TrackLocation_ShouldNotReportUntrackedLocation() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        var loc1 = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 1));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        // Act
        tracker.trackLocation(loc);
        // Assert
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
    }

    @Test
    void RemoveLocation_ShouldTrackRemoveTrackedLocation() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        tracker.trackLocation(loc);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        // Act
        tracker.removeLocation(loc);
        // Assert
        Assertions.assertFalse(tracker.isLocationTracked(loc));
    }

    @Test
    void Clear_ShouldUntrackAllLocations() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        var loc1 = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 1));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        tracker.trackLocation(loc);
        tracker.trackLocation(loc1);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        Assertions.assertTrue(tracker.isLocationTracked(loc1));
        // Act
        tracker.clear();
        // Assert
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
    }

    @Test
    void ClearDims_ShouldUntrackAllLocationsInDim() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        var loc1 = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 1));
        var loc2 = new Loc4Indirect(new ResourceLocation("stay"), new BlockPos(0, 0, 1));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        Assertions.assertFalse(tracker.isLocationTracked(loc2));
        tracker.trackLocation(loc);
        tracker.trackLocation(loc1);
        tracker.trackLocation(loc2);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        Assertions.assertTrue(tracker.isLocationTracked(loc1));
        Assertions.assertTrue(tracker.isLocationTracked(loc2));
        // Act
        tracker.clearDim(TestableLoc4.DEFAULT_DIM);
        // Assert
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        Assertions.assertTrue(tracker.isLocationTracked(loc2));
    }

    @Test
    void GetTrackedLocationsInDim_ReportsOnlyTrackedLocations() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        var loc1 = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 1));
        var loc2 = new Loc4Indirect(new ResourceLocation("stay"), new BlockPos(0, 0, 1));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        Assertions.assertFalse(tracker.isLocationTracked(loc2));
        tracker.trackLocation(loc);
        tracker.trackLocation(loc1);
        tracker.trackLocation(loc2);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        Assertions.assertTrue(tracker.isLocationTracked(loc1));
        Assertions.assertTrue(tracker.isLocationTracked(loc2));
        // Act & Assert
        Assertions.assertTrue(tracker.getTrackedLocationsInDim(TestableLoc4.DEFAULT_DIM)
                .allMatch((i) ->
                        (i.equals(loc) || i.equals(loc1)) && (!i.equals(loc2))));
        Assertions.assertTrue(tracker.getTrackedLocationsInDim(new ResourceLocation("stay"))
                .allMatch((i) ->
                        !(i.equals(loc) || i.equals(loc1)) && (i.equals(loc2))));
    }

    @Test
    void GetLocationsInRange_ReturnLocation() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        tracker.trackLocation(loc);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        // Act
        var locs = tracker.getLocationsInRange(new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 0)), 25f);
        // Assert
        locs.findFirst().ifPresentOrElse(
                (l) -> Assertions.assertEquals(0, l.compareTo(loc)),
                () -> Assertions.fail("Failed to find loc."));
    }

    @Test
    void GetLocationsInRange_NotReturnLocationOutOfRange() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 30));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        tracker.trackLocation(loc);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        // Act
        var locs = tracker.getLocationsInRange(new Loc4Indirect(TestableLoc4.DEFAULT_DIM, new BlockPos(0, 0, 0)), 25f);
        // Assert
        locs.findFirst().ifPresent((i) -> Assertions.fail("Found loc we shouldn't."));
    }

    @Test
    void GetLocationsInRange_OnBlockCoords_ShouldNotDieOnBigRanges() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        var loc1 = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 3000000));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        tracker.trackLocation(loc);
        tracker.trackLocation(loc1);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        Assertions.assertTrue(tracker.isLocationTracked(loc1));

        var executor = Executors.newFixedThreadPool(1);
        // Act
        var trackedLocs = executor.submit(() ->
                tracker.getLocationsInRange(TestableLoc4.ORIGIN, 5000000));
        var start = System.currentTimeMillis();
        var timeout = Duration.ofSeconds(5);
        var end = start + timeout.toMillis();
        while (!trackedLocs.isDone() && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Assert
        var done = trackedLocs.isDone();
        Assertions.assertTrue(done);
        Assertions.assertTrue(System.currentTimeMillis() < end);
        if (done) {
            Stream<Loc4> locs = null;
            try {
                locs = trackedLocs.get();
                Assertions.assertTrue(locs.allMatch((l) -> l.equals(loc) || l.equals(loc1)));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void GetLocationsInRange_OnPlayerCoords_ShouldNotDieOnBigRanges() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = TestableLoc4.ORIGIN;
        var loc1 = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 3000000));
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        tracker.trackLocation(loc);
        tracker.trackLocation(loc1);
        Assertions.assertTrue(tracker.isLocationTracked(loc));
        Assertions.assertTrue(tracker.isLocationTracked(loc1));

        var executor = Executors.newFixedThreadPool(1);
        // Act
        var trackedLocs = executor.submit(() ->
                tracker.getLocationsInRange(TestableLoc4.DEFAULT_DIM, new Vector3f(0, 0, 0), 5000000));
        var start = System.currentTimeMillis();
        var timeout = Duration.ofSeconds(5);
        var end = start + timeout.toMillis();
        while (!trackedLocs.isDone() && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Assert
        var done = trackedLocs.isDone();
        Assertions.assertTrue(done);
        Assertions.assertTrue(System.currentTimeMillis() < end);
        if (done) {
            Stream<Loc4> locs = null;
            try {
                locs = trackedLocs.get();
                Assertions.assertTrue(locs.allMatch((l) -> l.equals(loc) || l.equals(loc1)));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}