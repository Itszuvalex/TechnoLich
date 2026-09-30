package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.IModule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class TestableIItemStack implements IItemStack {
    public static final String ITEM_KEY = "Item";
    public static final String STACK_KEY = "Stack";
    public static final String DAMAGE_KEY = "Damage";
    public static final String NBT_KEY = "NBT";

    public static final Codec<IItemStack> CODEC = RecordCodecBuilder.<TestableIItemStack>create((instance) -> instance.group(
            Codec.INT.fieldOf(ITEM_KEY).forGetter((s) -> s.testItem),
            Codec.INT.fieldOf(STACK_KEY).forGetter((s) -> s.testStack),
            Codec.INT.fieldOf(DAMAGE_KEY).forGetter((s) -> s.testDamage),
            CompoundTag.CODEC.optionalFieldOf(NBT_KEY).forGetter((s) -> Optional.ofNullable(s.testNBT))
    ).apply(instance, (item, stack, damage, nbt) -> {
        var ret = new TestableIItemStack(item, stack, damage);
        ret.testNBT = nbt.orElse(null);
        return ret;
    })).xmap((s) -> s, (s) -> s instanceof TestableIItemStack t ? t : new TestableIItemStack());

    public static void overrideCodec() {
        IItemStack.CODEC.setOverrideValue(CODEC);
    }

    public static void resetCodec() {
        IItemStack.CODEC.revert();
    }

    public int testItem;
    public int testStack;
    public int testDamage;
    public int testStackMax = 64;
    public int testDamageMax = 0;
    public CompoundTag testNBT = new CompoundTag();

    public TestableIItemStack() {
        this(-1, 0, 0);
    }

    public TestableIItemStack(int item) {
        this(item, 1, 0);
    }

    public TestableIItemStack(int item, int stack) {
        this(item, stack, 0);
    }

    public TestableIItemStack(int item, int stack, int damage) {
        this.testItem = item;
        this.testStack = stack;
        this.testDamage = damage;
    }

    @Override
    public Identifier item() {
        return Identifier.fromNamespaceAndPath(TechnoLich.NAMELOWER, String.valueOf(testItem));
    }

    @Override
    public int stackSize() {
        return testStack;
    }

    @Override
    public void setStackSize(int size) {
        testStack = size;
    }

    @Override
    public int stackSizeMax() {
        return testStackMax;
    }

    @Override
    public int damage() {
        return testDamage;
    }

    @Override
    public void setDamage(int damage) {
        testDamage = damage;
    }

    @Override
    public int damageMax() {
        return testDamageMax;
    }

    @Override
    public @NotNull DataComponentPatch components() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public @NotNull ItemStack toMinecraft() {
        Assertions.fail("toMinecraft shouldn't be reached from test apis");
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        return stackSize() <= 0;
    }

    @Override
    public @NotNull IItemStack copy() {
        var ret = new TestableIItemStack(testItem, testStack, testDamage);
        ret.testNBT = Optional.ofNullable(testNBT).map(CompoundTag::copy).orElse(null);
        return ret;
    }

    @Override
    public boolean isItemEqual(@NotNull IItemStack other) {
        if (!(other instanceof TestableIItemStack)) {
            Assertions.fail("Tested TestableIItemStack#isItemEqual against non TestableIItemStack class: " + other.getClass().getTypeName());
            return false;
        }
        var testo = (TestableIItemStack) other;
        if (testItem != testo.testItem) return false;
        if (testDamage != testo.testDamage) return false;
        boolean nbtNullOrEmpty = testNBT == null || testNBT.isEmpty();
        boolean otherNbtNullOrEmpty = testo.testNBT == null || testo.testNBT.isEmpty();
        return nbtNullOrEmpty == otherNbtNullOrEmpty;
    }

    @Override
    public @NotNull <T> Optional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return Optional.empty();
    }
}
