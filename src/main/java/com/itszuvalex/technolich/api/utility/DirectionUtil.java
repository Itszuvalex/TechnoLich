package com.itszuvalex.technolich.api.utility;

import net.minecraft.core.Direction;

public class DirectionUtil {
    public static Direction DEFAULT_HORIZONTAL_FACING = Direction.NORTH;

    public static Direction getAbsoluteDirectionFromHorizontalRelative(Direction relative, Direction front) {
        return switch(relative) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default ->
                    switch(front) {
                        case EAST -> relative.getClockWise();
                        case SOUTH -> relative.getOpposite();
                        case WEST -> relative.getCounterClockWise();
                        // case NORTH -> // equivalent to default
                        default -> relative;
                    };
        };
    }

    public static Direction getHorizontalRelativeDirectionFromAbsolute(Direction absolute, Direction relative) {
        return switch (relative) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default ->
                    switch(absolute) {
                        case EAST -> relative.getCounterClockWise();
                        case SOUTH -> relative.getOpposite();
                        case WEST -> relative.getClockWise();
                        // case NORTH -> // equivalent to default
                        default -> relative;
                    };
        };
    }
}
