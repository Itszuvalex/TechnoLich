package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.api.Modules;
import com.itszuvalex.technolich.api.adapters.IBlockEntity;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.itszuvalex.technolich.api.utility.NBTSerializationScope;
import com.itszuvalex.technolich.util.Color;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class FragColorable extends BlockEntityFragment<Color> {
    public static String COLOR_TAG = "color";

    private Color color;

    public FragColorable(Color color) {
        this.color = color;
    }

    public FragColorable() {
        this(new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0));
    }

    @Override
    public void serializeTo(NBTSerializationScope scope, @NotNull ValueOutput output) {
        output.putInt(COLOR_TAG, color.toInt());
    }

    @Override
    public void deserialize(@NotNull ValueInput input, NBTSerializationScope scope) {
        var ci = input.getIntOr(COLOR_TAG, 0);
        color = new Color(ci);
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
    @NotNull Function<Direction, Color> faceToModuleMapper(@NotNull IBlockEntity be) {
        return (d) -> color;
    }

    @Override
    public @NotNull String name() {
        return "Colorable";
    }
}
