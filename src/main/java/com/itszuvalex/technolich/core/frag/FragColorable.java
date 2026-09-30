package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.api.Modules;
import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IColorable;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.util.Color;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Gives a block entity a color, exposed on every side through {@link Modules#COLORABLE}.  Setting it saves and syncs
 * to clients.
 */
public class FragColorable extends BlockEntityFragment<IColorable> implements IColorable {
    public static final String COLOR_TAG = "color";

    private @NotNull
    @Nonnull
    Color color;

    public FragColorable(@NotNull @Nonnull Color color) {
        this.color = color;
    }

    public FragColorable() {
        this(Color.TRANSPARENT);
    }

    @Override
    public @NotNull Color getColor() {
        return color;
    }

    @Override
    public void setColor(@NotNull Color color) {
        if (color.equals(this.color)) return;
        this.color = color;
        markDirtyAndSync();
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull ValueOutput output) {
        output.putInt(COLOR_TAG, color.toInt());
    }

    @Override
    public void deserialize(@NotNull ValueInput input, NBTSerializationScope scope) {
        color = new Color(input.getIntOr(COLOR_TAG, 0));
    }

    @Override
    public boolean handlesScope(NBTSerializationScope scope) {
        return true;
    }

    @Override
    public @NotNull IModule<IColorable> module() {
        return Modules.COLORABLE;
    }

    @Override
    public @Nonnull
    @NotNull Function<Direction, IColorable> faceToModuleMapper(@NotNull IBlockEntity be) {
        return (d) -> this;
    }

    @Override
    public @NotNull String name() {
        return "Colorable";
    }
}
