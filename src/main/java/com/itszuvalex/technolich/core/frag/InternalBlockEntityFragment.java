package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.core.IInternalBlockEntityFragment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public abstract class InternalBlockEntityFragment implements IInternalBlockEntityFragment {
    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull ValueOutput output) {

    }

    @Override
    public void deserialize(@NotNull ValueInput input, NBTSerializationScope scope) {

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
