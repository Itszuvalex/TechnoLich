package com.itszuvalex.technolich.core

import com.itszuvalex.technolich.MCAssert
import com.itszuvalex.technolich.TestIO
import com.itszuvalex.technolich.TestableLevel
import com.itszuvalex.technolich.TestableLoc4
import com.itszuvalex.technolich.api.adapters.IBlockEntity
import com.itszuvalex.technolich.api.adapters.IModule
import com.itszuvalex.technolich.api.utility.Loc4
import com.itszuvalex.technolich.api.utility.NBTSerializationScope
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.entity.BlockEntity
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A fake [IBlockEntity] whose only behaviour is hosting one [FragMultiblockPart], wired exactly as a real
 * [BlockEntityCore] subclass would (through a real [BlockEntityFragmentCollection]).
 */
class TestableMultiblockBlockEntity(private val pos: BlockPos, roles: List<MultiblockRoleRef>) : IBlockEntity {
    val host = TestableFragmentHost()
    private val fragList = BlockEntityFragmentCollection(host)
    val part = FragMultiblockPart(roles)

    init {
        fragList.addFragment(part)
    }

    override fun getBlockPos(): BlockPos = pos
    override fun toMinecraft(): BlockEntity = MCAssert.failVanillaClass("toMinecraft")
    override fun <T : Any> getModule(module: IModule<T>, side: Direction?): T? = fragList.getModule(module, side)
}

class MultiblockManagerTest {
    private val level = TestableLevel(TestableLoc4.DEFAULT_DIM)
    private val destroyed = ArrayList<BlockPos>()
    private val manager = MultiblockManager { _, pos -> destroyed.add(pos) }

    private fun place(shape: MultiblockShape, pos: BlockPos, vararg roles: String): TestableMultiblockBlockEntity {
        val entity = TestableMultiblockBlockEntity(pos, roles.map { MultiblockRoleRef(shape, it) })
        level.setIBlockEntity(entity)
        return entity
    }

    private fun load(entity: TestableMultiblockBlockEntity) = manager.onPartLoaded(Loc4.of(level, entity.getBlockPos()), entity.part)

    @Test
    fun OnPartLoaded_AllSlotsPresentAndMatching_JoinsEveryMemberToTheSameStructure() {
        val core = place(PAIR, BlockPos.ZERO, "core")
        val wing = place(PAIR, BlockPos(1, 0, 0), "wing")
        load(core)
        load(wing)

        val coreMembership = core.part.membership
        val wingMembership = wing.part.membership
        Assertions.assertNotNull(coreMembership)
        Assertions.assertNotNull(wingMembership)
        Assertions.assertEquals(coreMembership!!.structureId, wingMembership!!.structureId)
        Assertions.assertEquals(BlockPos.ZERO, coreMembership.offset)
        Assertions.assertEquals(BlockPos(1, 0, 0), wingMembership.offset)
        Assertions.assertSame(core.part, manager.get(coreMembership.structureId)!!.memberAt(BlockPos.ZERO))
    }

    /**
     * [TRIPLE] requires a `wing` at both (1,0,0) and (-1,0,0) — two separate mandatory slots sharing a role, not
     * alternatives. A part's own candidate search must still land on the right one: `leftWing` loads first, so its
     * search must reject offset (1,0,0) (wrong anchor, nothing there) before trying (-1,0,0), where it actually sits.
     */
    @Test
    fun OnPartLoaded_RoleAtMultipleOffsets_TriesEachCandidateOffsetUntilOneMatches() {
        val core = place(TRIPLE, BlockPos.ZERO, "core")
        val rightWing = place(TRIPLE, BlockPos(1, 0, 0), "wing")
        val leftWing = place(TRIPLE, BlockPos(-1, 0, 0), "wing")

        load(leftWing)
        load(core)
        load(rightWing)

        val id = core.part.membership?.structureId
        Assertions.assertNotNull(id)
        Assertions.assertEquals(id, rightWing.part.membership?.structureId)
        Assertions.assertEquals(id, leftWing.part.membership?.structureId)
        Assertions.assertEquals(BlockPos(1, 0, 0), rightWing.part.membership?.offset)
        Assertions.assertEquals(BlockPos(-1, 0, 0), leftWing.part.membership?.offset)
    }

    @Test
    fun OnPartLoaded_MissingNeighbor_DoesNotForm() {
        val core = place(PAIR, BlockPos.ZERO, "core")
        load(core)
        Assertions.assertNull(core.part.membership)
    }

    @Test
    fun OnPartLoaded_WrongRoleAtSlot_DoesNotForm() {
        val wrongRoleAtCorePos = place(PAIR, BlockPos.ZERO, "wing") // PAIR needs "core" here, not "wing"
        val wing = place(PAIR, BlockPos(1, 0, 0), "wing")
        load(wrongRoleAtCorePos)
        load(wing)
        Assertions.assertNull(wrongRoleAtCorePos.part.membership)
        Assertions.assertNull(wing.part.membership)
    }

    @Test
    fun OnPartRemoved_BreaksStructure_OtherMembersLeave() {
        val core = place(PAIR, BlockPos.ZERO, "core")
        val wing = place(PAIR, BlockPos(1, 0, 0), "wing")
        load(core)
        load(wing)
        val id = core.part.membership!!.structureId

        manager.onPartRemoved(level, wing.getBlockPos(), wing.part)
        Assertions.assertTrue(destroyed.isEmpty(), "DISSOLVE must not destroy any block")

        Assertions.assertNull(core.part.membership, "the other member must leave when a sibling is removed")
        Assertions.assertNull(manager.get(id), "the destroyed structure's instance must be forgotten")
    }

    @Test
    fun OnPartRemoved_DestroyAll_DestroysEveryOtherMember() {
        val core = place(LINKED_TRIPLE, BlockPos.ZERO, "core")
        val east = place(LINKED_TRIPLE, BlockPos(1, 0, 0), "wing")
        val west = place(LINKED_TRIPLE, BlockPos(-1, 0, 0), "wing")
        load(core)
        val id = core.part.membership!!.structureId

        manager.onPartRemoved(level, east.getBlockPos(), east.part)

        Assertions.assertEquals(setOf(BlockPos.ZERO, BlockPos(-1, 0, 0)), destroyed.toSet())
        Assertions.assertNull(core.part.membership)
        Assertions.assertNull(west.part.membership)
        Assertions.assertNull(manager.get(id))
    }

    /**
     * The sibling's chunk unloaded, so the manager no longer holds it; it must still be found (through the level,
     * which loads chunks) and destroyed rather than left as an orphaned piece.
     */
    @Test
    fun OnPartRemoved_DestroyAll_SiblingUnloaded_StillDestroyed() {
        val core = place(LINKED_TRIPLE, BlockPos.ZERO, "core")
        val east = place(LINKED_TRIPLE, BlockPos(1, 0, 0), "wing")
        val west = place(LINKED_TRIPLE, BlockPos(-1, 0, 0), "wing")
        load(core)
        manager.onPartUnloaded(west.part)

        manager.onPartRemoved(level, east.getBlockPos(), east.part)

        Assertions.assertTrue(BlockPos(-1, 0, 0) in destroyed, "unloaded sibling must be destroyed")
        Assertions.assertNull(west.part.membership)
    }

    @Test
    fun OnPartRemoved_DestroyAll_LeavesBlocksOfOtherStructures() {
        val core = place(LINKED_TRIPLE, BlockPos.ZERO, "core")
        val east = place(LINKED_TRIPLE, BlockPos(1, 0, 0), "wing")
        place(LINKED_TRIPLE, BlockPos(-1, 0, 0), "wing")
        load(core)
        // The west slot now holds a part of some other structure.
        val stranger = place(LINKED_TRIPLE, BlockPos(-1, 0, 0), "wing")
        stranger.part.join(MultiblockMembership(UUID.randomUUID(), LINKED_TRIPLE, BlockPos(-1, 0, 0)))

        manager.onPartRemoved(level, east.getBlockPos(), east.part)

        Assertions.assertEquals(listOf(BlockPos.ZERO), destroyed)
        Assertions.assertNotNull(stranger.part.membership)
    }

    /**
     * Destroying a sibling reports that sibling's own removal back to the manager; that must not tear anything down
     * twice.
     */
    @Test
    fun OnPartRemoved_DestroyAll_ReentrantRemovalIsIgnored() {
        val core = place(LINKED_TRIPLE, BlockPos.ZERO, "core")
        val east = place(LINKED_TRIPLE, BlockPos(1, 0, 0), "wing")
        val west = place(LINKED_TRIPLE, BlockPos(-1, 0, 0), "wing")
        val calls = ArrayList<BlockPos>()
        lateinit var mgr: MultiblockManager
        mgr = MultiblockManager { lvl, pos ->
            calls.add(pos)
            // Vanilla runs the destroyed block's removal hook, which reports back to the manager.
            val part = (lvl.getIBlockEntity(pos) as TestableMultiblockBlockEntity).part
            mgr.onPartRemoved(lvl, pos, part)
        }
        mgr.onPartLoaded(Loc4.of(level, BlockPos.ZERO), core.part)

        mgr.onPartRemoved(level, east.getBlockPos(), east.part)

        Assertions.assertEquals(listOf(BlockPos.ZERO, BlockPos(-1, 0, 0)).toSet(), calls.toSet())
        Assertions.assertEquals(2, calls.size)
        Assertions.assertNull(west.part.membership)
    }

    @Test
    fun OnPartUnloaded_DoesNotBreakStructure_JustDeregistersLocally() {
        val core = place(PAIR, BlockPos.ZERO, "core")
        val wing = place(PAIR, BlockPos(1, 0, 0), "wing")
        load(core)
        load(wing)
        val id = core.part.membership!!.structureId

        manager.onPartUnloaded(wing.part)

        Assertions.assertNotNull(core.part.membership, "unloading a sibling must not clear this member's membership")
        Assertions.assertNotNull(wing.part.membership, "unloading must not clear the unloaded member's own persisted membership")
        Assertions.assertNull(manager.get(id)!!.memberAt(BlockPos(1, 0, 0)), "unloaded member must not be resolvable through the instance")
    }

    /**
     * Models a server restart: a fresh [MultiblockManager] with nothing in memory, but a member whose
     * [FragMultiblockPart.membership] is already populated (standing in for a value just restored by [deserialize]).
     * Re-announcing must not mint a new structure id or require any other member to be present.
     */
    @Test
    fun OnPartLoaded_WithExistingMembership_RejoinsWithoutReformingOrNewId() {
        val core = place(PAIR, BlockPos.ZERO, "core")
        val wing = place(PAIR, BlockPos(1, 0, 0), "wing")
        load(core)
        load(wing)
        val id = core.part.membership!!.structureId

        val restarted = MultiblockManager()
        restarted.onPartLoaded(Loc4.of(level, BlockPos.ZERO), core.part)

        Assertions.assertEquals(id, core.part.membership!!.structureId, "reload must not mint a new id")
        Assertions.assertSame(core.part, restarted.get(id)!!.memberAt(BlockPos.ZERO))
    }

    @Test
    fun FragMultiblockPart_SerializeDeserialize_RoundTripsMembership() {
        val part = FragMultiblockPart(listOf(MultiblockRoleRef(PAIR, "core")))
        part.join(MultiblockMembership(UUID.randomUUID(), PAIR, BlockPos(1, 2, 3)))

        val tag = TestIO.write { part.serializeTo(NBTSerializationScope.LEVEL, it) }
        val loaded = FragMultiblockPart(listOf(MultiblockRoleRef(PAIR, "core")))
        loaded.deserialize(TestIO.read(tag), NBTSerializationScope.LEVEL)

        Assertions.assertEquals(part.membership, loaded.membership)
    }

    companion object {
        private lateinit var PAIR: MultiblockShape
        private lateinit var TRIPLE: MultiblockShape
        private lateinit var LINKED_TRIPLE: MultiblockShape

        @BeforeAll
        @JvmStatic
        fun classSetup() {
            PAIR = MultiblockShape.register(
                Identifier.fromNamespaceAndPath("technolich_test", "pair"),
                mapOf(BlockPos.ZERO to "core", BlockPos(1, 0, 0) to "wing"),
            )
            TRIPLE = MultiblockShape.register(
                Identifier.fromNamespaceAndPath("technolich_test", "triple"),
                mapOf(BlockPos.ZERO to "core", BlockPos(1, 0, 0) to "wing", BlockPos(-1, 0, 0) to "wing"),
            )
            LINKED_TRIPLE = MultiblockShape.register(
                Identifier.fromNamespaceAndPath("technolich_test", "linked_triple"),
                mapOf(BlockPos.ZERO to "core", BlockPos(1, 0, 0) to "wing", BlockPos(-1, 0, 0) to "wing"),
                MultiblockBreakPolicy.DESTROY_ALL,
            )
        }

        @AfterAll
        @JvmStatic
        fun classTeardown() = MultiblockShape.clear()
    }
}

class MultiblockShapeTest {
    @Test
    fun Register_DuplicateId_Throws() {
        val id = Identifier.fromNamespaceAndPath("technolich_test", "dup")
        MultiblockShape.register(id, mapOf(BlockPos.ZERO to "a"))
        try {
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                MultiblockShape.register(id, mapOf(BlockPos.ZERO to "a"))
            }
        } finally {
            MultiblockShape.clear()
        }
    }

    @Test
    fun Register_EmptySlots_Throws() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            MultiblockShape.register(Identifier.fromNamespaceAndPath("technolich_test", "empty"), emptyMap())
        }
    }

    @Test
    fun ById_UnknownId_ReturnsNull() {
        Assertions.assertNull(MultiblockShape.byId(Identifier.fromNamespaceAndPath("technolich_test", "never_registered")))
    }
}
