package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.api.utility.ScopedCompoundTagSerialization;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockEntityCore extends BlockEntity implements IBlockEntity, IBlockEntityBlockEventHandler, ScopedCompoundTagSerialization {
    public static final String FRAG_KEY = "frags";

    protected final @NotNull
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
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var ret = super.save(tag);
        serializeTo(NBTSerializationScope.LEVEL, ret);
        return ret;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        deserialize(tag, NBTSerializationScope.LEVEL);
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull CompoundTag tag) {
        tag.put(FRAG_KEY, fragList.serialize(scope));
    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {
        if (nbt.contains(FRAG_KEY))
            fragList.deserialize(nbt.getCompound(FRAG_KEY), scope);
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return fragList.handlesScope(scope);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fragList.rehydrateFrags();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fragList.invalidateFrags();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if (!handlesScope(NBTSerializationScope.DESCRIPTION)) return null;

        //Write your data into the tag
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (!handlesScope(NBTSerializationScope.DESCRIPTION)) return;
        CompoundTag tag = pkt.getTag();
        if (tag == null) return;

        deserialize(tag, NBTSerializationScope.DESCRIPTION);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        deserialize(tag, NBTSerializationScope.DESCRIPTION);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return serialize(NBTSerializationScope.DESCRIPTION);
    }

    @Override
    public void onRemove(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState blockStatePrev) {
        fragList.onRemove(level, pos, blockStatePrev);
    }
}
