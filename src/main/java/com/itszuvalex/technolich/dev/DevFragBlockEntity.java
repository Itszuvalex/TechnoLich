package com.itszuvalex.technolich.dev;

import com.itszuvalex.technolich.api.storage.IItemStorage;
import com.itszuvalex.technolich.api.storage.ItemStorageArray;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.api.wrappers.WrapperResourceHandlerIItemStorage;
import com.itszuvalex.technolich.core.BlockEntityCore;
import com.itszuvalex.technolich.core.frag.FragColorable;
import com.itszuvalex.technolich.core.frag.FragDropInventory;
import com.itszuvalex.technolich.core.frag.InternalBlockEntityFragment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

/**
 * A colorable block with a one-slot inventory that drops on removal and is exposed to other mods.
 */
public class DevFragBlockEntity extends BlockEntityCore {
    public final FragColorable colorable = new FragColorable();
    public final IItemStorage inventory = new ItemStorageArray(1, this::markDirty);
    public final ResourceHandler<ItemResource> itemHandler = WrapperResourceHandlerIItemStorage.of(inventory);

    public DevFragBlockEntity(BlockPos pos, BlockState state) {
        super(DevContent.DEV_FRAG_BLOCK_ENTITY.get(), pos, state);
        fragList.addFragment(colorable);
        fragList.addInternalFragment(new FragDropInventory(inventory));
        fragList.addInternalFragment(new InternalBlockEntityFragment() {
            @Override
            public @NotNull String name() {
                return "Inventory";
            }

            @Override
            public boolean handlesScope(NBTSerializationScope scope) {
                return scope == NBTSerializationScope.LEVEL;
            }

            @Override
            public void serializeTo(NBTSerializationScope scope, @NotNull ValueOutput output) {
                inventory.serialize(output);
            }

            @Override
            public void deserialize(@NotNull ValueInput input, NBTSerializationScope scope) {
                inventory.deserialize(input);
            }
        });
        fragList.addCapability(Capabilities.Item.BLOCK, (side) -> itemHandler);
    }
}
