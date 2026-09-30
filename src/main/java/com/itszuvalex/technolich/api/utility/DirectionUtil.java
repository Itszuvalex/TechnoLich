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

    /**
     * Inverse of {@link #getAbsoluteDirectionFromHorizontalRelative}.
     */
    public static Direction getHorizontalRelativeDirectionFromAbsolute(Direction absolute, Direction front) {
        return switch (absolute) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default ->
                    switch(front) {
                        case EAST -> absolute.getCounterClockWise();
                        case SOUTH -> absolute.getOpposite();
                        case WEST -> absolute.getClockWise();
                        // case NORTH -> // equivalent to default
                        default -> absolute;
                    };
        };
    }
}
