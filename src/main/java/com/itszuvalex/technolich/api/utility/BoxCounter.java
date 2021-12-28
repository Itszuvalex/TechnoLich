package com.itszuvalex.technolich.api.utility;

public class BoxCounter {
    private int boxed;

    public BoxCounter(int toBox) {
        boxed = toBox;
    }

    public int get() { return boxed; }
    public void set(int toBox) { boxed = toBox; }
}
