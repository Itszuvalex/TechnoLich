package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocationTrackerTest {

    @Test
    void TrackLocation_ShouldTrackLocation() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 0));
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
        var loc = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 0));
        var loc1 = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 1));
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
        var loc = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 0));
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
        var loc = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 0));
        var loc1 = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 1));
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
        var loc = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 0));
        var loc1 = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 1));
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
        tracker.clearDim(new ResourceLocation("test"));
        // Assert
        Assertions.assertFalse(tracker.isLocationTracked(loc));
        Assertions.assertFalse(tracker.isLocationTracked(loc1));
        Assertions.assertTrue(tracker.isLocationTracked(loc2));
    }

    @Test
    void GetTrackedLocationsInDim_ReportsOnlyTrackedLocations() {
        // Arrange
        var tracker = new LocationTracker();
        var loc = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 0));
        var loc1 = new Loc4Indirect(new ResourceLocation("test"), new BlockPos(0, 0, 1));
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
        Assertions.assertTrue(tracker.getTrackedLocationsInDim(new ResourceLocation("test"))
                .allMatch((i) ->
                        (i.equals(loc) || i.equals(loc1)) && (!i.equals(loc2))));
        Assertions.assertTrue(tracker.getTrackedLocationsInDim(new ResourceLocation("stay"))
                .allMatch((i) ->
                        !(i.equals(loc) || i.equals(loc1)) && (i.equals(loc2))));
    }

}