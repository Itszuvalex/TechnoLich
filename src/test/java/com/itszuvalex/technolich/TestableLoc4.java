package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.utility.Loc4;
import com.itszuvalex.technolich.api.utility.Loc4Indirect;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TestableLoc4 {
    public static @Nonnull
    @NotNull ResourceLocation DEFAULT_DIM = new ResourceLocation("test");
    public static @Nonnull
    @NotNull
    Loc4 ORIGIN = new Loc4Indirect(
            DEFAULT_DIM, new BlockPos(0, 0, 0));
}
