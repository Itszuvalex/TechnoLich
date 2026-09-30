package com.itszuvalex.technolich.dev;

import com.itszuvalex.technolich.TechnoLich;
import com.itszuvalex.technolich.api.Capabilities;
import com.itszuvalex.technolich.api.Modules;
import com.itszuvalex.technolich.api.adapters.IItemStack;
import com.itszuvalex.technolich.api.adapters.ILevel;
import com.itszuvalex.technolich.api.utility.Loc4;
import com.itszuvalex.technolich.api.wrappers.WrapperBlockEntity;
import com.itszuvalex.technolich.util.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.function.Consumer;

/**
 * Game tests for the block entity framework, run with {@code ./gradlew runGameTestServer}.
 */
public final class DevGameTests {
    private static final BlockPos POS = BlockPos.ZERO;

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, TechnoLich.NAMELOWER);

    private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> TESTS = List.of(
            TEST_FUNCTIONS.register("colorable_capability", () -> DevGameTests::colorableCapability),
            TEST_FUNCTIONS.register("level_save_load", () -> DevGameTests::levelSaveLoad),
            TEST_FUNCTIONS.register("client_update_tag", () -> DevGameTests::clientUpdateTag),
            TEST_FUNCTIONS.register("item_capability_insert", () -> DevGameTests::itemCapabilityInsert),
            TEST_FUNCTIONS.register("drops_inventory_on_break", () -> DevGameTests::dropsInventoryOnBreak),
            TEST_FUNCTIONS.register("level_lookup_returns_core", () -> DevGameTests::levelLookupReturnsCore),
            TEST_FUNCTIONS.register("inventory_change_marks_dirty", () -> DevGameTests::inventoryChangeMarksDirty)
    );

    private DevGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
        modEventBus.addListener(DevGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(TechnoLich.NAMELOWER, "default"));
        TESTS.forEach((test) -> event.registerTest(test.getId(), new FunctionGameTestInstance(test.getKey(),
                new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 100, 0, true))));
    }

    private static DevFragBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, DevContent.DEV_FRAG_BLOCK.get());
        return helper.getBlockEntity(POS, DevFragBlockEntity.class);
    }

    private static void colorableCapability(GameTestHelper helper) {
        var be = place(helper);
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);

        Color color = level.getCapability(Capabilities.COLORABLE, pos, null);
        helper.assertTrue(color != null, "COLORABLE capability missing");
        color.red = 42;
        helper.assertValueEqual(color, be.getModule(Modules.COLORABLE, null).orElseThrow(), "BlockEntityCore#getModule");
        helper.assertValueEqual(color, new WrapperBlockEntity(be).getModule(Modules.COLORABLE, null).orElseThrow(),
                "WrapperBlockEntity#getModule");
        helper.assertTrue(level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, pos, null) == null,
                "Unexposed capability should be null");
        helper.succeed();
    }

    private static void levelSaveLoad(GameTestHelper helper) {
        var be = place(helper);
        var registries = helper.getLevel().registryAccess();
        be.getModule(Modules.COLORABLE, null).orElseThrow().green = 7;
        be.inventory.setSlot(0, IItemStack.of(new ItemStack(Items.DIAMOND, 3)));

        var tag = be.saveWithFullMetadata(registries);
        var loaded = BlockEntity.loadStatic(be.getBlockPos(), be.getBlockState(), tag, registries);

        helper.assertTrue(loaded instanceof DevFragBlockEntity, "Loaded wrong block entity: " + loaded);
        var copy = (DevFragBlockEntity) loaded;
        helper.assertValueEqual(be.getModule(Modules.COLORABLE, null).orElseThrow().toInt(),
                copy.getModule(Modules.COLORABLE, null).orElseThrow().toInt(), "color");
        helper.assertValueEqual(3, copy.inventory.get(0).stackSize(), "inventory count");
        helper.assertTrue(copy.inventory.get(0).toMinecraft().is(Items.DIAMOND), "inventory item");
        helper.succeed();
    }

    private static void clientUpdateTag(GameTestHelper helper) {
        var be = place(helper);
        var registries = helper.getLevel().registryAccess();
        be.getModule(Modules.COLORABLE, null).orElseThrow().blue = 99;
        be.inventory.setSlot(0, IItemStack.of(new ItemStack(Items.DIAMOND)));

        var tag = be.getUpdateTag(registries);
        var client = new DevFragBlockEntity(be.getBlockPos(), be.getBlockState());
        client.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));

        helper.assertValueEqual((byte) 99, client.getModule(Modules.COLORABLE, null).orElseThrow().blue, "synced color");
        helper.assertTrue(client.inventory.get(0).isEmpty(), "Inventory is LEVEL scope only and must not sync");
        helper.succeed();
    }

    private static void itemCapabilityInsert(GameTestHelper helper) {
        var be = place(helper);
        var handler = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK,
                helper.absolutePos(POS), null);
        helper.assertTrue(handler != null, "Item BLOCK capability missing");

        try (var tx = Transaction.openRoot()) {
            handler.insert(ItemResource.of(Items.DIAMOND), 5, tx);
            // Aborted: must roll back
        }
        helper.assertTrue(be.inventory.get(0).isEmpty(), "Aborted transaction was not rolled back");

        try (var tx = Transaction.openRoot()) {
            helper.assertValueEqual(5, handler.insert(ItemResource.of(Items.DIAMOND), 5, tx), "inserted");
            tx.commit();
        }
        helper.assertValueEqual(5, be.inventory.get(0).stackSize(), "committed count");
        helper.succeed();
    }

    private static void dropsInventoryOnBreak(GameTestHelper helper) {
        var be = place(helper);
        be.inventory.setSlot(0, IItemStack.of(new ItemStack(Items.DIAMOND, 2)));
        helper.destroyBlock(POS);
        helper.succeedWhen(() -> helper.assertItemEntityPresent(Items.DIAMOND, POS, 2.0));
    }

    /**
     * Location lookups must hand back the BlockEntityCore itself, not a capability-only wrapper, so modules without a
     * block capability (e.g. network modules) stay reachable.
     */
    private static void levelLookupReturnsCore(GameTestHelper helper) {
        var be = place(helper);
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);

        helper.assertTrue(ILevel.of(level).getIBlockEntity(pos) == be, "ILevel#getIBlockEntity wrapped the core");
        helper.assertTrue(Loc4.of(level, pos).getIBlockEntity(false).orElseThrow() == be,
                "Loc4Level#getIBlockEntity wrapped the core");
        helper.assertTrue(Loc4.of(ILevel.of(level), pos).getIBlockEntity(false).orElseThrow() == be,
                "Loc4ILevel#getIBlockEntity wrapped the core");
        helper.succeed();
    }

    /**
     * Inventory changes must mark the block entity (and so its chunk) for saving: directly, and through the item
     * capability on commit only.
     */
    private static void inventoryChangeMarksDirty(GameTestHelper helper) {
        var be = place(helper);
        var pos = helper.absolutePos(POS);
        var chunk = helper.getLevel().getChunkAt(pos);
        var handler = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK, pos, null);
        helper.assertTrue(handler != null, "Item BLOCK capability missing");

        chunk.tryMarkSaved();
        be.inventory.setSlot(0, IItemStack.of(new ItemStack(Items.DIAMOND)));
        helper.assertTrue(chunk.isUnsaved(), "Direct inventory change did not mark dirty");

        chunk.tryMarkSaved();
        try (var tx = Transaction.openRoot()) {
            handler.insert(0, ItemResource.of(Items.DIAMOND), 1, tx);
        }
        helper.assertFalse(chunk.isUnsaved(), "Aborted transaction marked dirty");

        try (var tx = Transaction.openRoot()) {
            handler.insert(0, ItemResource.of(Items.DIAMOND), 1, tx);
            tx.commit();
        }
        helper.assertTrue(chunk.isUnsaved(), "Committed transaction did not mark dirty");
        helper.succeed();
    }
}
