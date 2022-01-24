package com.itszuvalex.technolich.util;

import java.util.function.Supplier;

public class Singleton<T> {
    private Supplier<T> instanceSupplier;
    private T instance;

    public Singleton(Supplier<T> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    public T get() {
        if (instance == null) {instance = instanceSupplier.get();}
        return instance;
    }

    public void reset() {
        instance = null;
    }

    public void setSupplier(Supplier<T> supplier) {
        instanceSupplier = supplier;
        reset();
    }
}
