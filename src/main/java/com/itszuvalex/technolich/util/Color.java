package com.itszuvalex.technolich.util;

/**
 * Immutable ARGB color.  Use the {@code with*} methods to derive a changed copy.
 */
public record Color(byte alpha, byte red, byte green, byte blue) {
    public static final Color TRANSPARENT = new Color(0);

    /**
     * @param argb Packed as {@code 0xAARRGGBB}.
     */
    public Color(int argb) {
        this((byte) (argb >>> 24), (byte) (argb >>> 16), (byte) (argb >>> 8), (byte) argb);
    }

    /**
     * @return Packed as {@code 0xAARRGGBB}.
     */
    public int toInt() {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    public Color withAlpha(byte alpha) {
        return new Color(alpha, red, green, blue);
    }

    public Color withRed(byte red) {
        return new Color(alpha, red, green, blue);
    }

    public Color withGreen(byte green) {
        return new Color(alpha, red, green, blue);
    }

    public Color withBlue(byte blue) {
        return new Color(alpha, red, green, blue);
    }
}
