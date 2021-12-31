package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ItemStorageAggregate implements IItemStorage {
    private @Nonnull
    @NotNull
    final IItemStorage[] storages;

    public ItemStorageAggregate(@Nonnull @NotNull IItemStorage[] storages) {
        this.storages = storages;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        return getStorageForIndex(index).get(getLocalStorageIndexForIndex(index));
    }

    @Override
    public int size() {
        return Arrays.stream(storages).map(IItemStorage::size).reduce(0, Integer::sum);
    }

    @Override
    public void setSlot(int index, @NotNull IItemStack stack) {
        getStorageForIndex(index).setSlot(getLocalStorageIndexForIndex(index), stack);
    }

    private @NotNull
    @Nonnull
    IItemStorage getStorageForIndex(int index) {
        var i = index;
        for (IItemStorage storage : storages) {
            if (i < storage.size()) {return storage;} else {i -= storage.size();}
        }
        return IItemStorage.Empty;
    }

    private int getLocalStorageIndexForIndex(int index) {
        var i = index;
        for (IItemStorage storage : storages) {
            if (i < storage.size()) {return i;} else {i -= storage.size();}
        }
        return 0;
    }
}
