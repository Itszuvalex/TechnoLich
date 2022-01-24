package com.itszuvalex.technolich.util;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.ILevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;

public class InventoryUtils implements IInventoryUtils {
    @Override
    public void dropItem(ILevel level, BlockPos pos, IItemStack item) {
        Containers.dropItemStack(level.toMinecraft(), pos.getX(), pos.getY(), pos.getZ(), item.toMinecraft());
    }
}
