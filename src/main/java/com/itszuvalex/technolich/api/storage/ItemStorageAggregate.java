package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.utility.BoxCounter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class ItemStorageAggregate implements IItemStorage {
    private @Nonnull @NotNull
    final IItemStorage[] storages;
    public ItemStorageAggregate(@Nonnull @NotNull IItemStorage[] storages) {
        this.storages = storages;
    }

    @Override
    public @NotNull IItemStack get(int index) {
        return null;
    }

    @Override
    public int size() {
        return Arrays.stream(storages).map(IItemStorage::size).reduce(0, Integer::sum);
    }

    @Override
    public void setSlot(int index, @NotNull IItemStack stack) {

    }

    private @NotNull @Nonnull
    IItemStorage getStorageForIndex(int index) {
        var i = index;
        for(IItemStorage storage : storages)
        {
            if (i < 0)
                return IItemStorage.Empty;
            if (i <= storage.size())
                return storage;
            else
                i -= storage.size();
        }
        return IItemStorage.Empty;
    }

    private int getStorageIndexForIndex(int index) {
        var i = index;
        var si = 0;
        for(IItemStorage storage : storages)
        {
            if (i < 0)
                return -1;
            if (i <= storage.size())
                return si;
            else
                i -= storage.size();

            si++;
        }
        return -1;
    }
}
