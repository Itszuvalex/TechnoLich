package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.core.IBlockEntityFragment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class BlockEntityFragment<T> implements IBlockEntityFragment<T> {
    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull CompoundTag tag) {

    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {

    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return false;
    }

    @Override
    public void onRemove(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState blockStatePrev) {

    }

    @Override
    public void invalidateFrags() {

    }

    @Override
    public void rehydrateFrags() {

    }
}
