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

    private LazyOptional<Color> color;

    public FragColorable(Color color) {
        this.color = LazyOptional.of(() -> color);
    }

    public FragColorable() {
        this(new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0));
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull CompoundTag tag) {
        tag.putInt(COLOR_TAG, color.resolve().get().toInt());
    }

    @Override
    public void deserialize(@NotNull CompoundTag nbt, NBTSerializationScope scope) {
        var lastcolor = color;
        var ci = nbt.getInt(COLOR_TAG);
        var newcolor = new Color(ci);
        color = LazyOptional.of(() -> newcolor);
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
        return (d) -> this::getColor;
    }

    private LazyOptional<Color> getColor() { return color; }

    @Override
    public @NotNull String name() {
        return "Colorable";
    }

    @Override
    public void invalidateFrags() {
        color.invalidate();
    }
}
