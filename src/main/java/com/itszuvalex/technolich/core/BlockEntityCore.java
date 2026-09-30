package com.itszuvalex.technolich.core;

import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.IScopedSerialization;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Optional;

public class BlockEntityCore extends BlockEntity implements IBlockEntity, IBlockEntityBlockEventHandler, IScopedSerialization, IFragmentHost {
    private static final Logger LOGGER = LogUtils.getLogger();
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
    public @NotNull IBlockEntity blockEntity() {
        return this;
    }

    @Override
    public void markDirty() {
        setChanged();
    }

    @Override
    public void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide() && handlesScope(NBTSerializationScope.DESCRIPTION)) {
            var state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @NotNull <T> Optional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return fragList.getModule(module, side);
    }

    /**
     * Capability provider target.  Register with {@link com.itszuvalex.technolich.api.ModuleCapabilities}.
     */
    public <T> @Nullable T getCapability(@NotNull @Nonnull BlockCapability<T, Direction> cap, @Nullable Direction side) {
        return fragList.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        serializeTo(NBTSerializationScope.LEVEL, output);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        deserialize(input, NBTSerializationScope.LEVEL);
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull ValueOutput output) {
        fragList.serializeTo(scope, output.child(FRAG_KEY));
    }

    @Override
    public void deserialize(@NotNull ValueInput input, NBTSerializationScope scope) {
        input.child(FRAG_KEY).ifPresent((frags) -> fragList.deserialize(frags, scope));
        // Fragments may have replaced the objects they expose.
        if (level != null) invalidateCapabilities();
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return fragList.handlesScope(scope);
    }

    @Override
    public void clearRemoved() {
        fragList.rehydrateFrags();
        super.clearRemoved();
    }

    @Override
    public void setRemoved() {
        fragList.invalidateFrags();
        super.setRemoved();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if (!handlesScope(NBTSerializationScope.DESCRIPTION)) return null;

        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ValueInput input) {
        if (!handlesScope(NBTSerializationScope.DESCRIPTION)) return;

        deserialize(input, NBTSerializationScope.DESCRIPTION);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        deserialize(input, NBTSerializationScope.DESCRIPTION);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
            var output = TagValueOutput.createWithContext(reporter, registries);
            serializeTo(NBTSerializationScope.DESCRIPTION, output);
            return output.buildResult();
        }
    }

    @Override
    public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) onRemove(ILevel.of(level), pos, state);
    }

    @Override
    public void onRemove(@NotNull ILevel level, @NotNull BlockPos pos, @NotNull BlockState blockStatePrev) {
        fragList.onRemove(level, pos, blockStatePrev);
    }
}
