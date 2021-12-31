package com.itszuvalex.technolich.api.storage;

class ItemStorageAggregateTest extends ItemStorageTestBase {

    @Override
    public IItemStorage storageWithSize(int size) {
        int size1 = (int) Math.ceil(size / 2f);
        int size2 = (int) Math.floor(size / 2f);

        IItemStorage[] storages = size <= 1 ?
                new IItemStorage[]{new ItemStorageArray(size)} :
                new IItemStorage[]{
                        new ItemStorageArray(size1),
                        new ItemStorageArray(size2)
                };

        return new ItemStorageAggregate(storages);
    }
}