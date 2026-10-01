# AGENTS.md

Guidance for coding agents (Claude Code, Codex, etc.) and humans working in this repository. `CLAUDE.md` is a symlink to this file; edit this one.

## What this is

TechnoLich is a "techy-magic" Minecraft mod. Today it is almost entirely **framework**: block entities composed of fragments, a module/capability layer, scoped serialization, item/energy storage abstractions, and block entity networks. It registers no gameplay content yet; the only block is dev-only test content (see [Dev content and game tests](#dev-content-and-game-tests)).

- **Minecraft 26.1.2 / NeoForge 26.1.2.112 / Java 25**, built with ModDevGradle (`net.neoforged.moddev`).
- **Written in Kotlin 2.4.0**, loaded by [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge) 6.3.0 (`kff_version`, `modLoader="kotlinforforge"`). KFF is a **required mod at runtime**: it supplies the Kotlin stdlib, so players must install it alongside TechnoLich. The build pulls it from the KFF maven.
- Ported from Forge 1.18.1 (`1.18.1-39.0.8`, Java) on the `neoforge-26.1` branch; `main` still holds the Forge version.
- The same framework, in Kotlin, is shared with ItszuLib (https://github.com/Itszuvalex/ItszuLib, branch `neoforge-26.1`, package `com.itszuvalex.itszulib`). When changing framework code here, make the matching change there.
- Mod id `technolich`, base package `com.itszuvalex.technolich`, MIT licensed.

## Build and run

```bash
./gradlew build                 # compile + unit tests + jar (build/libs/technolich-<version>.jar)
./gradlew test                  # JUnit unit tests only
./gradlew runGameTestServer     # in-game tests; exits non-zero if a required test fails
./gradlew runClient             # dev client (dev content is registered)
./gradlew runServer             # dev dedicated server (run/eula.txt must say eula=true)
```

The build targets a JDK 25 toolchain, and NeoForge's tooling also uses JDK 21. By default Gradle auto-detects installed JDKs and downloads missing ones through the foojay resolver (`settings.gradle`). `gradlew` itself needs Java 17+ on `PATH` or `JAVA_HOME` to start.

Machine-specific JDK settings go in the user-level `~/.gradle/gradle.properties` (or `$GRADLE_USER_HOME/gradle.properties`), never in the project's `gradle.properties`. NixOS can't run the generic-Linux JDKs Gradle downloads, so point it at Nix JDKs there:

```properties
org.gradle.java.home=/home/cchharris/.gradle/jdks/jdk25
org.gradle.java.installations.paths=/home/cchharris/.gradle/jdks/jdk25,/home/cchharris/.gradle/jdks/jdk21
org.gradle.java.installations.auto-download=false
```

Mod metadata is generated from `src/main/templates/META-INF/neoforge.mods.toml` using the `mod_*` properties in `gradle.properties`.

## Documentation

NeoForge's API changes a lot between versions and many online examples are stale. Prefer these sources, in order:

0. **`~/Repos/neoforge-docs/CHEATSHEET.md`** — a short, port-tested reference distilled from doing
   this port (capabilities, transactions, Value I/O, data components, renames, game tests, the
   NixOS toolchain fix). Not a substitute for the real docs, but check it before diving into the
   mirror below for anything that sounds like a Forge→NeoForge rename or gotcha.
1. **Decompiled, NeoForge-patched Minecraft sources** (always exactly the version we build against): `build/moddev/artifacts/minecraft-patched-<version>-sources.jar` (created by any Gradle build). NeoForge classes: the `neoforge-<version>-universal.jar` in the Gradle cache (`~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/`); use `javap` to check signatures.
2. **NeoForge docs**: https://docs.neoforged.net/docs/ (the unversioned pages are 26.1). Key pages for this codebase: Capabilities (`inventories/capabilities`), Transactions (`inventories/transactions`), Value I/O (`datastorage/valueio`), Block Entities (`blockentities/`), Game Tests (`misc/gametest`), Networking (`networking/`).
3. **Porting primers**: https://docs.neoforged.net/primer/docs/ — per-version Minecraft change lists (e.g. `1.21.5` for Value I/O, `26.1` for the latest renames).
4. **ModDevGradle docs**: https://docs.neoforged.net/toolchain/docs/plugins/mdg/ (runs, unit tests, game tests).
5. **Forge docs (historical)**: https://docs.minecraftforge.net/en/1.18.x/ describes the Forge 1.18 APIs the original code (and the older ItszuLib) was written against. Useful for understanding intent; do not use for current APIs.

The maintainer keeps offline mirrors of 2–5 at `~/Repos/neoforge-docs/html/` (all NeoForge doc versions, primers, toolchain) and `~/Repos/forge-docs/html/en/` (`1.18.x`, `latest`, `fg-6.x`).

## Source layout

Related types share a file, so look for the file named after the group, not the class.

```
src/main/kotlin/com/itszuvalex/technolich/
├── TechnoLich.kt          @Mod object (KFF): module init, data components, dev content (non-production only),
│                          server tick / chunk unload / server stop → NETWORK_MANAGER
├── api/
│   ├── Api.kt             Capabilities (COLORABLE), Modules (COLORABLE; Modules.init()), Components
│   │                      (FRAGMENT_DATA = technolich:fragment_data), ModuleCapabilities (registers a
│   │                      BlockEntityCore type's modules + STANDARD NeoForge caps)
│   ├── adapters/          IModule.kt (IModule, IModuleProvider, Module), Adapters.kt (IBlockEntity, ILevel),
│   │                      IItemStack.kt, IColorable.kt
│   ├── storage/           IItemStorage.kt, IBattery.kt; ItemStorages.kt (Array, Slice, Aggregate, Dynamic, NBT,
│   │                      ResourceHandler-backed); Batteries.kt (PowerBattery, PowerBatteryNBT,
│   │                      DynamicIBattery, BatteryEnergyHandler)
│   ├── utility/           Loc4 (value type), ChunkCoord, LocationTracker, DirectionUtil,
│   │                      ModuleCapabilityMaps.kt, IScopedSerialization + NBTSerializationScope, Overideable,
│   │                      misc helpers
│   └── wrappers/          Wrappers.kt: Vanilla/NeoForge ↔ TechnoLich adapters (WrapperLevel, WrapperBlockEntity,
│                          WrapperVanillaItemStack, WrapperContainerIItemStorage,
│                          WrapperResourceHandlerIItemStorage, WrapperEnergyHandlerIBattery, WrapperCache, ...)
├── core/                  BlockEntityCore.kt (BlockEntityCore, TickableBlockEntityCore, EntityBlockCore,
│   │                      TickableEntityBlockCore), Fragments.kt (fragment interfaces, IFragmentHost,
│   │                      BlockEntityFragmentCollection), Networks.kt (INetwork, TileNetwork, NetworkManager,
│   │                      INetworkNode, TileNetworkNode, NetworkEdge), Multiblocks.kt (MultiblockShape,
│   │                      IMultiblockMember, MultiblockInstance, MultiblockManager, FragMultiblockPart),
│   │                      SidedStorageConfiguration.kt
│   └── frag/              FragmentBases.kt (InternalBlockEntityFragment, BlockEntityFragment),
│                          Frags.kt (FragColorable, FragDropInventory)
├── dev/                   DevContent.kt (dev-only blocks + block entities, DevShapes), DevGameTests.kt; never
│                          registered in production
├── network/PacketHandler.kt Thin wrapper over NeoForge's PayloadRegistrar
├── team/                  Teams.kt (Team, TeamState: rules + invariants), TeamData.kt (TeamDataType, Research),
│                          TeamCodec.kt, TeamStore.kt (file persistence), TeamManager.kt, TeamNetwork.kt (sync,
│                          lifecycle wiring), TeamCommands.kt (/technolich team, /technolich research)
└── util/                  Color, InventoryUtils.kt (item dropping)
src/test/kotlin/...        JUnit tests; fakes in TestHelpers.kt (TestableLevel, TestableIItemStack, TestableLoc4,
                           MCAssert, TestIO), core/CoreTests.kt (TestableNetwork*, TestableFragmentHost), and
                           core/MultiblockTests.kt (TestableMultiblockBlockEntity)
```

## Core concepts

### Fragments and BlockEntityCore
A `BlockEntityCore` owns a `BlockEntityFragmentCollection` (`fragList`). Behaviour is composed by adding fragments in the block entity constructor:

- `fragList.addFragment(IBlockEntityFragment<T>)`: a fragment that *exposes* a module `T` (e.g. `FragColorable` exposes itself as an `IColorable`). Its `faceToModuleMapper(be)` maps a nullable `Direction` to the current instance (or null). It is called on every query, so return live state. Expose an interface whose mutators save/sync (like the `IColorable.color` setter), never a mutable value object: other mods get the same object through the capability.
- `fragList.addInternalFragment(IInternalBlockEntityFragment)`: hooks without an exposed module (serialization, `onRemove`, e.g. `FragDropInventory`). Only `name` is required: every lifecycle hook (`IFragmentLifecycle`: `onLoad`, `onChunkUnloaded`, `onNeighborChanged`, `onRemove`, `invalidateFrags`, `rehydrateFrags`) defaults to a no-op, and serialization defaults to handling no scope.
- Fragment `name`s must be unique per block entity (they key saved data), and each module may be exposed by only one fragment; both throw `IllegalArgumentException` otherwise.
- Each fragment gets an `IFragmentHost` through `onAttach` when added (`BlockEntityCore` is the host). Fragments extending `InternalBlockEntityFragment`/`BlockEntityFragment` call `markDirty()` after changing saved state and `markDirtyAndSync()` after changing client-visible (DESCRIPTION) state; the latter also sends a block update to clients.
- Storages report their own changes: construct `ItemStorageArray`/`ItemStorageNBT`/`PowerBattery` with an `onChanged` runnable (e.g. `ItemStorageArray(1) { markDirty() }`) and every `setSlot`/`setStorage` runs it. `setSlotQuietly`/`setStorageQuietly` skip it; the NeoForge adapters use those inside transactions and call `setChanged()` once on root commit, so aborted transactions never dirty the block entity.
- `fragList.addCapability(BlockCapability<T, Direction>, side -> T)`: expose a non-module capability, typically one of `ModuleCapabilities.STANDARD` (NeoForge item/fluid/energy).
- `fragList.addTickable(...)`: ticked by `TickableBlockEntityCore` when the block's `TickableEntityBlockCore#hasTicker(side)` returns true.

`BlockEntityCore` wires fragments into the vanilla lifecycle: `saveAdditional`/`loadAdditional` (LEVEL scope), `getUpdateTag`/`handleUpdateTag`/`onDataPacket` (DESCRIPTION scope), `setRemoved`/`clearRemoved` (fragment invalidation), `onLoad`/`onChunkUnloaded` → fragment `onLoad`/`onChunkUnloaded` (each called once, with a live `ILevel`/`BlockPos`, for the matching vanilla event — `onLoad` fires both for a fresh placement and for a chunk load; see `Multiblocks.kt` for the main user of these), `EntityBlockCore.neighborChanged` → fragment `onNeighborChanged` (26.1 no longer says which neighbour changed), and `preRemoveSideEffects` → fragment `onRemove` (server only, only when the block actually changes).

### Modules and capabilities
An `IModule<T>` is a handle for a behaviour, identified by a namespaced `Identifier` (`id()`, e.g. `technolich:colorable`), optionally backed by a NeoForge `BlockCapability<T, Direction>` and/or `ItemCapability<T, ItemAccess>`. Register with `Module.registerModule(id, blockCap[, itemCap])`; ids must be unique (the registry is thread-safe for parallel mod construction).

- Internal lookups: `IModuleProvider.getModule(module, side)` returns `T?` (null when not exposed) (implemented by `BlockEntityCore`, `IItemStack`, `WrapperBlockEntity`, etc.).
- External lookups (other mods): standard NeoForge `level.getCapability(cap, pos, side)`. For this to work, **call `ModuleCapabilities.registerBlockEntity(event, type)` for every `BlockEntityCore` type** from a `RegisterCapabilitiesEvent` listener (mod bus). It registers every module's block capability plus `STANDARD`, all routed to `BlockEntityCore#getCapability`.
- Modules must be registered before `RegisterCapabilitiesEvent`; put them in static fields of a class that is loaded during mod construction (see `Modules.init()`).
- `BlockEntityCore#deserialize` calls `invalidateCapabilities()` after loading, since fragments may replace exposed objects. If a fragment swaps its exposed object at any other time, call `invalidateCapabilities()` on the block entity.

### Scoped serialization
`IScopedSerialization` (`serializeTo(scope, ValueOutput)`, `deserialize(ValueInput, scope)`, `handlesScope(scope)`) with `NBTSerializationScope`:
- `LEVEL`: world save.
- `DESCRIPTION`: client sync (chunk load and block update packets).
- `ITEM`: data that stays with the block's item form. `BlockEntityCore#collectImplicitComponents` writes it into the `technolich:fragment_data` component (`Components.FRAGMENT_DATA`) and `applyImplicitComponents` restores it on placement. Creative pick-block (with data) carries it automatically; for survival drops the block's loot table must copy it: `{"function": "minecraft:copy_components", "source": "block_entity", "include": ["technolich:fragment_data"]}`. Keep inventories out of ITEM scope; they drop their contents instead.

Each fragment writes into its own child keyed by `name` under the `frags` key. Use `ValueOutput`/`ValueInput` (and `Codec`s via `store`/`read`), not raw `CompoundTag`, for anything saved with a block entity.

### Items and storage
- `IItemStack` wraps `ItemStack` (`WrapperVanillaItemStack`, `IItemStack.of(stack)`, `IItemStack.Empty`). It is persisted with `IItemStack.codec()`, an `Overideable<Codec<IItemStack>>` whose default wraps `ItemStack.OPTIONAL_CODEC`. Tests override it with `TestableIItemStack.CODEC`. `components()` returns the stack's `DataComponentPatch` (items no longer carry NBT).
- `IItemStorage` is slot-based with rich default transfer logic and `ValueIOSerializable` persistence. `ItemStorageNBT` is a live view over a `CompoundTag` (e.g. inside `CUSTOM_DATA`); pass a `HolderLookup.Provider` when items may carry datapack-registry components.
- `IBattery` is a double-based energy store; `ValueIOSerializable`.

### NeoForge transfer API adapters
NeoForge 26.1 replaced `IItemHandler`/`IEnergyStorage` with transactional `ResourceHandler<ItemResource>` and `EnergyHandler`.
- TechnoLich → NeoForge: `WrapperResourceHandlerIItemStorage.of(storage)` (one NeoForge `ItemStackResourceHandler` per slot; honours `canInsert` and per-slot `maxStackSize`), `WrapperEnergyHandlerIBattery(battery)` (snapshot journal; energy is truncated to whole units). Create once per block entity and return the same instance from capability providers.
- NeoForge → TechnoLich: `ItemStorageResourceHandler`, `BatteryEnergyHandler`. Their mutators open **root** transactions, so never call them while a transaction is open.

### Networks
`INetwork`/`TileNetwork` group `INetworkNode`s (located by `Loc4`) into server-side networks managed by `NetworkManager` (`TechnoLich.NETWORK_MANAGER`, a plain instance that only server-side networks register with; ticked from `ServerTickEvent.Pre/Post`, cleared on server stop). Nodes are found through a network module on the block entity (`TileNetwork#networkModule`), looked up in the level `TileNetwork#levelFor(dimension)` returns (the running server's level by default; tests override it). `LocationTracker` indexes locations by dimension and chunk. See the KDoc on `INetwork` for the design rationale. Node and edge collections are `Sequence`s.

Lifecycle: `TechnoLich` forwards server `ChunkEvent.Unload` to `NetworkManager#onChunkUnload`, which drops that chunk's nodes as a batch; block entities must re-add their node when they load. Removed nodes get `onRemoved`, absorbed networks get `onTakeover`, and `addNode` moves a node out of any previous network first. Connecting two nodes pulls both (and their networks) into the network doing the connecting. Splitting explores iteratively, so long cable lines are safe. `INetwork` is the caller-facing contract; splitting, merging and creating sub-networks are `TileNetwork` internals, with `create()` (abstract), `canAddNode`, `onSplit` and `onTakeover` as the `protected` hooks a subclass implements or overrides.

### Multiblocks
There is deliberately no single "controller" block holding a multiblock's state. A `MultiblockShape` (`MultiblockShape.register(id, slots[, breakPolicy])`) is a map of relative offset → role name; the offset (0,0,0) is just a coordinate reference, not a privileged position — it need not even be occupied. Any block entity can be a member by exposing `IMultiblockMember` (`candidateRoles`, `membership`, `join`/`leave`) through the module/capability system; `FragMultiblockPart(candidateRoles)` is the ready-made fragment for `BlockEntityCore` subclasses — add it with `fragList.addFragment(...)` like any other fragment, and it is reachable externally for free via the existing `ModuleCapabilities.registerBlockEntity` call. Members of one structure need not share a block, a block entity type, or even a mod.

Formation is driven entirely by each member's own `onLoad` (see [Fragments and BlockEntityCore](#fragments-and-blockentitycore) above), not by scanning or polling: a member with no `membership` tries every shape+offset its `candidateRoles` could occupy, which needs every slot's chunk loaded at that one moment (the usual one-time cost of validating a shape) but nothing afterward — a loaded member never needs its siblings loaded to answer for itself. `MultiblockManager` (`TechnoLich.MULTIBLOCK_MANAGER`, server-only) mints a `UUID` structure id once, on formation, and it stays stable regardless of unrelated chunk load/unload churn. A member's own chunk unloading (`onChunkUnloaded`) just deregisters it locally without telling anyone, since its own saved `membership` already remembers it for next load.

What breaking a member (`onRemove`) does depends on the shape's `MultiblockBreakPolicy`:
- `DISSOLVE` (default): the structure breaks and every other *currently loaded* member `leave()`s; the other blocks stay in the world and may form again.
- `DESTROY_ALL`: every other slot is looked up through the level (which loads its chunk) and each block still in this structure leaves and is destroyed (`Level#destroyBlock`, with drops), so no orphaned pieces remain in unloaded chunks. The removals this causes report back to `MultiblockManager` and are ignored, since the structure is already being torn down.

**Known limitation** (`DISSOLVE` only): if a member is destroyed while a sibling is in an unloaded chunk, that sibling is not told, and keeps believing it belongs to a dead structure until it is itself broken. See the KDoc on `MultiblockManager` for the (deliberately not yet built) fix — a small, level-scoped record of retired structure ids.

### Teams and per-team data
Every player is always in exactly one team; a new player gets a solo team they own. Per-team data (`TeamDataType`: codec, empty value, `merge` on join, `copy` on leave) is registered in `TeamDataTypes` during mod construction; `Research` (a set of unlocked ids, merged by union) is the first. Joining unions the joiner's data into the team; leaving or being removed gives the player a solo team with a copy; disbanding gives every member a copy. Roles: one owner (promotes/demotes officers, hands over ownership, renames, disbands; cannot leave a shared team without handing it over), officers (invite, revoke invites, remove anyone but the owner), members. Joining is by invite and accept.

Data integrity rules (do not weaken them):
- `TeamState` is immutable and validated on construction (`TeamState.of`): each player in exactly one team, one owner per team, no empty teams, no self-invites. Operations return a new state or throw (`TeamException` for a refused request), so nothing is half-applied. `TeamState.repaired` fixes invalid saved data deterministically and logs each repair.
- `TechnoLich.TEAMS` (`TeamManager`) is the only writer: `change { state -> newState }` on the server thread; it marks the data dirty and notifies listeners (the client sync).
- Persistence is `TeamStore`, deliberately not vanilla `SavedData` (which replaces unreadable data with a fresh empty instance and later saves it over the file). Decoding is strict (`TeamCodec`): any malformed field fails the whole load. An unreadable file falls back to `teams.dat.bak` (the bad file is moved to `teams.dat.corrupt-<time>`); if both are unreadable both are left alone and the store refuses to save for the session. Saves write `teams.dat.tmp`, read it back, copy the old file to `.bak`, then atomically move. Unregistered data types are kept raw and written back.
- Stored at `<world>/data/technolich/teams.dat`; loaded on `ServerStartingEvent`, saved on the overworld's `LevelEvent.Save` and on `ServerStoppedEvent`. Clients get their own team (`TeamSyncPayload` → `ClientTeam.current`) at login and after every change to it.

### Engine seams for testing
`ILevel`, `IBlockEntity`, `IItemStack` and `Overideable` exist so logic can be unit tested without a running game. Tests use `TestableLevel`, `TestableIItemStack`, `TestableLoc4`, `TestableNetwork*`, `TestableFragmentHost`; `MCAssert.failVanillaClass` marks methods that must not be reached in tests, and `TestIO` round-trips Value I/O.

## Dev content and game tests

`dev/` registers `technolich:dev_frag_block` (colorable, 1-slot inventory exposed via `Capabilities.Item.BLOCK`, drops on break) and a two-block multiblock pair, `technolich:dev_multiblock_core` + `technolich:dev_multiblock_wing` (`DevShapes.PAIR`: `core` at the origin, `wing` one block east), only when `!FMLEnvironment.isProduction()`. Place with `/setblock ~ ~ ~ technolich:dev_frag_block` (etc.) in a dev client.

`DevGameTests` registers test functions (`Registries.TEST_FUNCTION`) and test instances (`RegisterGameTestsEvent`) using vanilla's 1×1×1 `minecraft:empty` structure. Current tests: capability/module lookup (and color setter marking dirty), level save/load, client update tag, item capability insert with rollback, drops on break, location lookups returning the core itself, inventory changes marking dirty (commit only), ITEM-scope component round trip, the item ResourceHandler's per-slot limit, and the multiblock pair forming/breaking. Note `GameTestHelper#assertValueEqual(expected, actual, name)`: expected comes first. The multiblock tests use `helper.startSequence()`/`thenWaitUntil`/`thenIdle` rather than asserting immediately, since `onLoad` (what drives formation) is deferred by vanilla to the tick after placement, not called synchronously from `setBlock`.

## Testing conventions

- Unit tests: `src/test/kotlin`, JUnit 5, names like `Method_ExpectedBehavior`. ModDevGradle's unit-test support puts Minecraft classes on the classpath, but anything needing registries or a level belongs in a game test.
- Add a game test for any behaviour that crosses into vanilla/NeoForge (serialization with real items, capabilities, block lifecycle).
- Verify with `./gradlew build` **and** `./gradlew runGameTestServer`.

## Kotlin conventions

- The `@Mod` class is a Kotlin `object`. Use `thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS` for the mod event bus and `NeoForge.EVENT_BUS` for game events.
- Registries are `DeferredRegister`s in `object`s, registered from `TechnoLich`'s `init`.
- Nullability lives in the types: return `T?` for "may be absent" (no `Optional`, no `@NotNull`/`@Nullable` annotations). Keep NeoForge-facing capability providers nullable where NeoForge expects it.
- Prefer `Sequence` over `Stream`, and properties for simple getters (`Loc4.x`, `IModule.id`, `Color.red`).
- Storage and battery classes are `open` so block entities can subclass them (e.g. to override `maxStackSize`).
- Add `@JvmField`/`@JvmStatic`/`@JvmOverloads` where Java callers or reflection need plain fields, statics or default arguments.
- Keep vanilla/NeoForge types out of `api/adapters` interfaces where a TechnoLich abstraction already exists.
