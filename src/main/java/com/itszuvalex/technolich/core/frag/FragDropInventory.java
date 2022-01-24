package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.storage.IItemStorage;
import com.itszuvalex.technolich.util.IInventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public class FragDropInventory extends InternalBlockEntityFragment {
    public static String name = "Drop Inventory";
    private final IItemStorage storage;
    public boolean shouldDrop = true;

    public FragDropInventory(IItemStorage storage) {
        this.storage = storage;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public void onRemove(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState blockStatePrev) {
        if (shouldDrop) {
            IInventoryUtils.instance.get().dropStorage(level, pos, storage);
            IntStream.range(0, storage.size()).forEach((i) -> storage.setSlot(i, IItemStack.Empty));
        }
    }
}
