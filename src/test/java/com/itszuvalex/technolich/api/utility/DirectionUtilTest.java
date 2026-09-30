package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DirectionUtilTest {
    private static final List<Direction> HORIZONTAL = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    @Test
    void GetHorizontalRelativeDirectionFromAbsolute_NorthFront_IsIdentity() {
        for (var dir : Direction.values()) {
            Assertions.assertEquals(dir, DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(dir, Direction.NORTH));
        }
    }

    @Test
    void GetHorizontalRelativeDirectionFromAbsolute_UpDown_Unchanged() {
        for (var front : HORIZONTAL) {
            Assertions.assertEquals(Direction.UP, DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(Direction.UP, front));
            Assertions.assertEquals(Direction.DOWN, DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(Direction.DOWN, front));
        }
    }

    @Test
    void GetHorizontalRelativeDirectionFromAbsolute_FrontFace_IsRelativeNorth() {
        for (var front : HORIZONTAL) {
            Assertions.assertEquals(Direction.NORTH, DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(front, front));
        }
    }

    @Test
    void GetHorizontalRelativeDirectionFromAbsolute_InvertsGetAbsoluteDirectionFromHorizontalRelative() {
        for (var front : HORIZONTAL) {
            for (var dir : Direction.values()) {
                var absolute = DirectionUtil.getAbsoluteDirectionFromHorizontalRelative(dir, front);
                Assertions.assertEquals(dir, DirectionUtil.getHorizontalRelativeDirectionFromAbsolute(absolute, front),
                        "front=" + front + " relative=" + dir);
            }
        }
    }
}
