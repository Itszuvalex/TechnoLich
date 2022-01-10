package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.api.Modules;
import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.core.IBlockEntityFragment;
import com.itszuvalex.technolich.util.Color;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public class FragColorable implements IBlockEntityFragment<Color> {
    public static String COLOR_TAG = "color";

    private Color color;
    private LazyOptional<Color> colorOpt;

    public FragColorable(Color color) {
        this.color = color;
        this.colorOpt = LazyOptional.of(() -> color);
    }

    public FragColorable() {
        this(new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0));
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull CompoundTag tag) {
        tag.putInt(COLOR_TAG, color.toInt());
    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {
        var lastcolor = colorOpt;
        var ci = nbt.getInt(COLOR_TAG);
        color = new Color(ci);
        colorOpt = LazyOptional.of(() -> color);
        lastcolor.invalidate();
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return true;
    }

    @Override
    public @NotNull IModule<Color> module() {
        return Modules.COLORABLE;
    }

    @Override
    public @Nonnull
    @NotNull Function<Direction, Supplier<LazyOptional<Color>>> faceToModuleSupplierMapper(@NotNull IBlockEntity be) {
        return (d) -> this::getColorOpt;
    }

    private LazyOptional<Color> getColorOpt() {
        return colorOpt;
    }

    @Override
    public @NotNull String name() {
        return "Colorable";
    }

    @Override
    public void invalidateFrags() {
        colorOpt.invalidate();
    }

    @Override
    public void rehydrateFrags() {
        colorOpt = LazyOptional.of(() -> color);
    }
}
