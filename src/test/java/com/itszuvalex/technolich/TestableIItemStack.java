package com.itszuvalex.technolich;

import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.IModule;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class TestableIItemStack implements IItemStack {
    public static final String ITEM_KEY = "Item";
    public static final String STACK_KEY = "Stack";
    public static final String DAMAGE_KEY = "Damage";
    public static final String NBT_KEY = "NBT";

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
    public ResourceLocation item() {
        return new ResourceLocation(TechnoLich.NAMELOWER, String.valueOf(testItem));
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
    public @Nullable CompoundTag nbt() {
        return testNBT;
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
        ret.testNBT = Optional.of(testNBT).orElse(null);
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
    public void writeToNBT(@NotNull CompoundTag nbt) {
        nbt.putInt(ITEM_KEY, testItem);
        nbt.putInt(STACK_KEY, testStack);
        nbt.putInt(DAMAGE_KEY, testDamage);
        if (testNBT != null)
            nbt.put(NBT_KEY, testNBT);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getModule(@NotNull IModule<T> module, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var ret = new CompoundTag();
        writeToNBT(ret);
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        testItem = nbt.getInt(ITEM_KEY);
        testStack = nbt.getInt(STACK_KEY);
        testDamage = nbt.getInt(DAMAGE_KEY);
        if (nbt.contains(NBT_KEY)) {
            testNBT = nbt.getCompound(NBT_KEY);
        } else {
            testNBT = null;
        }
    }
}
