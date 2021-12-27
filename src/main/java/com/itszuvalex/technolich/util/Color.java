package com.itszuvalex.technolich.util;

public class Color {
    public byte alpha;
    public byte red;
    public byte green;
    public byte blue;

    public Color(byte alpha, byte red, byte green, byte blue) {
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(int color) {
        this(
                (byte) (((color & (255 << 24)) >> 24) & 255),
                (byte) (((color & (255 << 16)) >> 16) & 255),
                (byte) (((color & (255 << 8)) >> 8) & 255),
                (byte) (color & 255)
        );
    }

    public int toInt() {
        int r1 = 0;
        r1 += (alpha & 255) << 24;
        r1 += (red & 255) << 16;
        r1 += (green & 255) << 8;
        r1 += blue & 255;
        return r1;
    }

    public Color setRed(byte red) {
        this.red = red;
        return this;
    }

    public Color setGreen(byte green) {
        this.green = green;
        return this;
    }

    public Color setBlue(byte blue) {
        this.blue = blue;
        return this;
    }

    public Color setAlpha(byte alpha) {
        this.alpha = alpha;
        return this;
    }
}
