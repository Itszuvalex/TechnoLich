package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.IScopedNBTSerialization;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockEntityCore extends BlockEntity implements IBlockEntity, IScopedNBTSerialization<CompoundTag> {
    public static final String FRAG_KEY = "frags";

    private final @NotNull
    @Nonnull
    BlockEntityFragmentCollection fragList;

    public BlockEntityCore(@NotNull @Nonnull BlockEntityType<?> type, @NotNull @Nonnull BlockPos pos, @NotNull @Nonnull BlockState state) {
        super(type, pos, state);
        fragList = new BlockEntityFragmentCollection(this);
    }

    @Override
    public @NotNull BlockEntity toMinecraft() {
        return this;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return fragList.getModule(module, side);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        var ret = super.getCapability(cap, side);
        if (ret.isPresent()) return ret;
        return fragList.getCapability(cap, side);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        var ret= super.save(tag);
        // TODO: Figure this out
        return ret;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        deserialize(tag, NBTSerializationScope.LEVEL);
    }

    @NotNull
    @Override
    public CompoundTag serialize(NBTSerializationScope scope) {
        CompoundTag tag = new CompoundTag();
        tag.put(FRAG_KEY, fragList.serialize(scope));
        return tag;
    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {
        if(nbt.contains(FRAG_KEY))
            fragList.deserialize(nbt.getCompound(FRAG_KEY), scope);
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return fragList.handlesScope(scope);
    }
}
