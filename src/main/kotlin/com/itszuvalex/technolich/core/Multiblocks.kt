package com.itszuvalex.technolich.core

import com.itszuvalex.technolich.TechnoLich
import com.itszuvalex.technolich.api.Modules
import com.itszuvalex.technolich.api.adapters.IBlockEntity
import com.itszuvalex.technolich.api.adapters.ILevel
import com.itszuvalex.technolich.api.adapters.IModule
import com.itszuvalex.technolich.api.utility.Loc4
import com.itszuvalex.technolich.api.utility.NBTSerializationScope
import com.itszuvalex.technolich.core.frag.BlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.TestOnly
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A declarative multiblock pattern: a set of relative-position "slots", each requiring a named role. The offset
 * (0,0,0) is just a coordinate reference, not a privileged "controller" position — it need not even be occupied.
 *
 * A role may occupy more than one slot (e.g. four `wall` slots); any part that can fill that role may occupy any of
 * them. Matching is axis-aligned and fixed-orientation only; a rotated/mirrored variant needs its own registered
 * shape (or its own offsets added to this one) — there is no automatic rotation search.
 *
 * [breakPolicy] decides what breaking one member does to the others.
 */
class MultiblockShape private constructor(
    val id: Identifier,
    slots: Map<BlockPos, String>,
    val breakPolicy: MultiblockBreakPolicy,
) {
    val slots: Map<BlockPos, String> = slots.toMap()

    /**
     * Offsets that may satisfy each role.
     */
    val offsetsByRole: Map<String, List<BlockPos>> = this.slots.entries.groupBy({ it.value }, { it.key })

    override fun toString(): String = "MultiblockShape[$id]"

    companion object {
        private val SHAPES = ConcurrentHashMap<Identifier, MultiblockShape>()

        /**
         * @throws IllegalArgumentException if [slots] is empty, or a shape with this id is already registered.
         */
        @JvmStatic
        @JvmOverloads
        fun register(
            id: Identifier,
            slots: Map<BlockPos, String>,
            breakPolicy: MultiblockBreakPolicy = MultiblockBreakPolicy.DISSOLVE,
        ): MultiblockShape {
            require(slots.isNotEmpty()) { "Shape $id has no slots" }
            val shape = MultiblockShape(id, slots, breakPolicy)
            require(SHAPES.putIfAbsent(id, shape) == null) { "Shape with id: $id already registered." }
            return shape
        }

        @JvmStatic
        fun byId(id: Identifier): MultiblockShape? = SHAPES[id]

        @TestOnly
        @JvmStatic
        fun clear() = SHAPES.clear()
    }
}

/**
 * What breaking one member of a formed structure does to the rest.
 */
enum class MultiblockBreakPolicy {
    /**
     * The structure dissolves: every other member stays in the world and leaves the structure, free to form again.
     * Members in unloaded chunks are not told (see [MultiblockManager]).
     */
    DISSOLVE,

    /**
     * Every other member is destroyed too. Siblings are looked up through the level, which loads their chunks, so
     * members in unloaded chunks are removed rather than left as orphaned pieces.
     */
    DESTROY_ALL,
}

/**
 * One role a [IMultiblockMember] can fill, in one [shape].
 */
data class MultiblockRoleRef(val shape: MultiblockShape, val role: String)

/**
 * A formed structure's identity plus which slot one particular member occupies. [structureId] is minted once, when
 * the structure forms, and is stable until it breaks — unrelated events (chunk load/unload, an unrelated structure
 * forming elsewhere) never change it.
 */
data class MultiblockMembership(val structureId: UUID, val shape: MultiblockShape, val offset: BlockPos)

/**
 * Something that can be a multiblock member. [FragMultiblockPart] is the fragment-based implementation for
 * [BlockEntityCore] subclasses; anything else that persists its own [membership] and calls [MultiblockManager]'s
 * `onPartLoaded`/`onPartUnloaded`/`onPartRemoved` at the right lifecycle points can participate too — the members of
 * one structure need not share a block, a block entity type, or even a mod.
 */
interface IMultiblockMember {
    val candidateRoles: List<MultiblockRoleRef>

    val membership: MultiblockMembership?

    /**
     * Called by [MultiblockManager], once, when this member's slot is validated as part of a newly forming
     * structure. Implementations persist [membership] (so a later [MultiblockManager.onPartLoaded] can restore it
     * without re-forming) and mark themselves dirty.
     */
    fun join(membership: MultiblockMembership)

    /**
     * Called by [MultiblockManager] when the structure this member belonged to is destroyed by another member
     * breaking. Implementations clear their stored membership and mark themselves dirty.
     */
    fun leave()
}

/**
 * One formed structure. Exists only in memory, rebuilt incrementally as members load: members persist their own
 * [MultiblockMembership] (see [FragMultiblockPart]), not this instance, so an instance with some members not yet
 * loaded is normal, not an error — see [MultiblockManager].
 */
class MultiblockInstance(val id: UUID, val shape: MultiblockShape, val anchor: Loc4) {
    private val members = HashMap<BlockPos, IMultiblockMember>()

    /**
     * @return The member at [offset] (relative to [anchor]), if currently loaded and registered.
     */
    fun memberAt(offset: BlockPos): IMultiblockMember? = members[offset]

    fun locFor(offset: BlockPos): Loc4 = anchor.getOffset(offset)

    /**
     * @return Every currently-loaded member, keyed by offset. Will be short of [MultiblockShape.slots] whenever part
     * of the structure is in an unloaded chunk; that is expected, not a sign the structure is broken.
     */
    fun loadedMembers(): Map<BlockPos, IMultiblockMember> = members

    internal fun isEmpty(): Boolean = members.isEmpty()

    internal fun register(offset: BlockPos, member: IMultiblockMember) {
        members[offset] = member
    }

    internal fun unregister(offset: BlockPos) {
        members.remove(offset)
    }

    internal fun membersExcept(offset: BlockPos): List<IMultiblockMember> = members.filterKeys { it != offset }.values.toList()
}

/**
 * Tracks formed multiblock structures. Server-only (see [TechnoLich.MULTIBLOCK_MANAGER]); like [NetworkManager], it
 * only ever has to be correct about currently-loaded members, never about the whole structure at once.
 *
 * Formation is driven entirely by each member's own lifecycle — there is no scanning or polling. A part with no
 * membership tries, on every [onPartLoaded], every shape+offset combination implied by its
 * [IMultiblockMember.candidateRoles]. That one check needs every slot's chunk loaded at that moment (the same
 * one-time cost any multiblock design pays to validate a shape), but costs nothing afterward: a loaded member never
 * needs its siblings loaded to answer for itself.
 *
 * Known limitation ([MultiblockBreakPolicy.DISSOLVE] only): if a member is destroyed while a sibling is unloaded,
 * that sibling is not told — it keeps believing it belongs to a dead [MultiblockMembership] until it is itself
 * broken or re-notified some other way. Fixing this needs a small, level-scoped record of retired structure ids
 * (cheap: just ids, no payload) that a reloading member can check itself against. Deliberately not built yet; add it
 * if break-while-sibling-unloaded turns out to matter in practice. [MultiblockBreakPolicy.DESTROY_ALL] has no such
 * gap: it loads the siblings' chunks to remove them.
 *
 * @param destroyBlock Removes a sibling's block for [MultiblockBreakPolicy.DESTROY_ALL] (as if broken, with drops).
 * Replaceable for tests.
 */
class MultiblockManager(
    private val destroyBlock: (ILevel, BlockPos) -> Unit = { level, pos -> level.toMinecraft().destroyBlock(pos, true) },
) {
    private val instances = HashMap<UUID, MultiblockInstance>()

    /**
     * Structures being torn down by [onPartRemoved], so the removals it causes don't recurse.
     */
    private val breaking = HashSet<UUID>()

    fun get(id: UUID): MultiblockInstance? = instances[id]

    fun clear() = instances.clear()

    /**
     * Called once when a member attaches to a loaded level (placement or chunk load). If it already remembers a
     * [MultiblockMembership], it just re-registers into that structure — creating the in-memory instance if this is
     * the first of its members to load this session — with no re-validation and no new id. Otherwise, tries to form
     * a new structure at every slot its [IMultiblockMember.candidateRoles] could occupy.
     */
    fun onPartLoaded(level: ILevel, pos: BlockPos, member: IMultiblockMember) {
        val loc = Loc4.of(level, pos)
        val membership = member.membership
        if (membership != null) {
            val instance = instances.getOrPut(membership.structureId) {
                MultiblockInstance(membership.structureId, membership.shape, anchorFor(loc, membership.offset))
            }
            instance.register(membership.offset, member)
            return
        }
        for (roleRef in member.candidateRoles) {
            val offsets = roleRef.shape.offsetsByRole[roleRef.role] ?: continue
            for (offset in offsets) {
                if (tryForm(level, roleRef.shape, anchorFor(loc, offset))) return
            }
        }
    }

    /**
     * The member's chunk unloaded; it is still part of its structure, just not resident. Does not notify any
     * sibling — the member's own saved data already remembers its membership for next time it loads.
     */
    fun onPartUnloaded(member: IMultiblockMember) {
        val membership = member.membership ?: return
        val instance = instances[membership.structureId] ?: return
        instance.unregister(membership.offset)
        if (instance.isEmpty()) instances.remove(membership.structureId)
    }

    /**
     * The member at [pos] was broken. Its structure no longer exists. What happens to the other members depends on
     * the shape's [MultiblockShape.breakPolicy]:
     * - [MultiblockBreakPolicy.DISSOLVE]: every other currently-loaded member is told to [IMultiblockMember.leave].
     * - [MultiblockBreakPolicy.DESTROY_ALL]: every other slot of the structure is looked up through [level] (loading
     *   its chunk if needed); each block there that still belongs to this structure leaves and is destroyed.
     */
    fun onPartRemoved(level: ILevel, pos: BlockPos, member: IMultiblockMember) {
        val membership = member.membership ?: return
        val id = membership.structureId
        // A sibling being destroyed below reports its own removal; the structure is already being torn down.
        if (!breaking.add(id)) return
        try {
            val instance = instances.remove(id)
            when (membership.shape.breakPolicy) {
                MultiblockBreakPolicy.DISSOLVE -> instance?.membersExcept(membership.offset)?.forEach { it.leave() }
                MultiblockBreakPolicy.DESTROY_ALL -> destroySiblings(level, pos, membership)
            }
        } finally {
            breaking.remove(id)
        }
    }

    private fun destroySiblings(level: ILevel, pos: BlockPos, membership: MultiblockMembership) {
        val anchor = pos.subtract(membership.offset)
        for (offset in membership.shape.slots.keys) {
            if (offset == membership.offset) continue
            val siblingPos = anchor.offset(offset)
            // Not Loc4.getIBlockEntity(level): the lookup must load the chunk, or an unloaded sibling is orphaned.
            val sibling = level.getIBlockEntity(siblingPos)?.getModule(Modules.MULTIBLOCK_MEMBER, null) ?: continue
            // Another structure (or a lone part) may have taken the slot since; leave it alone.
            if (sibling.membership?.structureId != membership.structureId) continue
            sibling.leave()
            destroyBlock(level, siblingPos)
        }
    }

    private fun anchorFor(memberLoc: Loc4, offset: BlockPos): Loc4 = memberLoc.getOffset(-offset.x, -offset.y, -offset.z)

    /**
     * Validates every slot of [shape] at [anchor] against currently-loaded, not-yet-joined members, and if all slots
     * match, mints a new structure id and joins every member to it.
     */
    private fun tryForm(level: ILevel, shape: MultiblockShape, anchor: Loc4): Boolean {
        val found = LinkedHashMap<BlockPos, IMultiblockMember>()
        for ((offset, role) in shape.slots) {
            val candidate = anchor.getOffset(offset).getIBlockEntity(level)?.getModule(Modules.MULTIBLOCK_MEMBER, null) ?: return false
            if (candidate.membership != null) return false
            if (MultiblockRoleRef(shape, role) !in candidate.candidateRoles) return false
            found[offset] = candidate
        }

        val id = UUID.randomUUID()
        val instance = MultiblockInstance(id, shape, anchor)
        found.forEach { (offset, member) ->
            instance.register(offset, member)
            member.join(MultiblockMembership(id, shape, offset))
        }
        instances[id] = instance
        return true
    }
}

/**
 * Fragment-based [IMultiblockMember] for [BlockEntityCore] subclasses. Persists [membership] (LEVEL scope only, as
 * a shape id, structure id, and offset) so a reload re-announces to [MultiblockManager] without re-forming. Exposed
 * through [Modules.MULTIBLOCK_MEMBER] like any other module, so [com.itszuvalex.technolich.api.ModuleCapabilities]
 * makes it reachable from outside the mod with no extra wiring.
 */
class FragMultiblockPart(override val candidateRoles: List<MultiblockRoleRef>) :
    BlockEntityFragment<IMultiblockMember>(), IMultiblockMember {
    override var membership: MultiblockMembership? = null
        private set

    override fun join(membership: MultiblockMembership) {
        this.membership = membership
        markDirty()
    }

    override fun leave() {
        membership = null
        markDirty()
    }

    override val module: IModule<IMultiblockMember> get() = Modules.MULTIBLOCK_MEMBER

    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IMultiblockMember? = { this }

    override val name: String get() = NAME

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope == NBTSerializationScope.LEVEL

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {
        val m = membership ?: return
        output.putString(ID_TAG, m.structureId.toString())
        output.putString(SHAPE_TAG, m.shape.id.toString())
        output.putInt(X_TAG, m.offset.x)
        output.putInt(Y_TAG, m.offset.y)
        output.putInt(Z_TAG, m.offset.z)
    }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        membership = runCatching {
            val idStr = input.getString(ID_TAG).orElse(null) ?: return@runCatching null
            val shapeStr = input.getString(SHAPE_TAG).orElse(null) ?: return@runCatching null
            val shape = MultiblockShape.byId(Identifier.parse(shapeStr)) ?: return@runCatching null
            MultiblockMembership(
                UUID.fromString(idStr),
                shape,
                BlockPos(input.getIntOr(X_TAG, 0), input.getIntOr(Y_TAG, 0), input.getIntOr(Z_TAG, 0)),
            )
        }.getOrNull()
    }

    override fun onLoad(level: ILevel, pos: BlockPos) {
        if (level.isClientSide) return
        TechnoLich.MULTIBLOCK_MANAGER.onPartLoaded(level, pos, this)
    }

    override fun onChunkUnloaded(level: ILevel, pos: BlockPos) {
        if (level.isClientSide) return
        TechnoLich.MULTIBLOCK_MANAGER.onPartUnloaded(this)
    }

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        if (level.isClientSide) return
        TechnoLich.MULTIBLOCK_MANAGER.onPartRemoved(level, pos, this)
    }

    companion object {
        const val NAME = "MultiblockPart"
        const val ID_TAG = "id"
        const val SHAPE_TAG = "shape"
        const val X_TAG = "x"
        const val Y_TAG = "y"
        const val Z_TAG = "z"
    }
}
