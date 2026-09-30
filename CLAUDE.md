# CLAUDE.md

Guidance for Claude Code (and humans) working in this repository.

## What this is

TechnoLich is a "techy-magic" Minecraft mod. Today it is almost entirely **framework**: block entities composed of fragments, a module/capability layer, scoped serialization, item/energy storage abstractions, and block entity networks. It registers no gameplay content yet; the only block is dev-only test content (see [Dev content and game tests](#dev-content-and-game-tests)).

- **Minecraft 26.1.2 / NeoForge 26.1.2.112 / Java 25**, built with ModDevGradle (`net.neoforged.moddev`).
- Ported from Forge 1.18.1 (`1.18.1-39.0.8`) on the `neoforge-26.1` branch; `master` still holds the Forge version.
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

1. **Decompiled, NeoForge-patched Minecraft sources** (always exactly the version we build against): `build/moddev/artifacts/minecraft-patched-<version>-sources.jar` (created by any Gradle build). NeoForge classes: the `neoforge-<version>-universal.jar` in the Gradle cache (`~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/`); use `javap` to check signatures.
2. **NeoForge docs**: https://docs.neoforged.net/docs/ (the unversioned pages are 26.1). Key pages for this codebase: Capabilities (`inventories/capabilities`), Transactions (`inventories/transactions`), Value I/O (`datastorage/valueio`), Block Entities (`blockentities/`), Game Tests (`misc/gametest`), Networking (`networking/`).
3. **Porting primers**: https://docs.neoforged.net/primer/docs/ — per-version Minecraft change lists (e.g. `1.21.5` for Value I/O, `26.1` for the latest renames).
4. **ModDevGradle docs**: https://docs.neoforged.net/toolchain/docs/plugins/mdg/ (runs, unit tests, game tests).
5. **Forge docs (historical)**: https://docs.minecraftforge.net/en/1.18.x/ describes the Forge 1.18 APIs the original code (and the older ItszuLib/Femtocraft mods) was written against. Useful for understanding intent; do not use for current APIs.

The maintainer keeps offline mirrors of 2–5 at `~/Repos/neoforge-docs/html/` (all NeoForge doc versions, primers, toolchain) and `~/Repos/forge-docs/html/en/` (`1.18.x`, `latest`, `fg-6.x`).

## Source layout

```
src/main/java/com/itszuvalex/technolich/
├── TechnoLich.java            @Mod entry: module init, dev content (non-production only), server tick → NetworkManager
├── api/
│   ├── Capabilities.java      TechnoLich block capabilities (COLORABLE)
│   ├── Modules.java           Built-in modules (COLORABLE); Modules.init() forces registration
│   ├── ModuleCapabilities.java Registers a BlockEntityCore type's modules + STANDARD NeoForge caps
│   ├── adapters/              Engine-facing interfaces: IModule/Module, IModuleProvider, IBlockEntity,
│   │                          ILevel, IItemStack, IBattery
│   ├── storage/               IItemStorage + implementations (Array, Slice, Aggregate, NBT, Dynamic,
│   │                          ResourceHandler-backed); IBattery implementations (PowerBattery,
│   │                          PowerBatteryNBT, DynamicIBattery, EnergyHandler-backed)
│   ├── utility/               Loc4 (+Level/ILevel/Indirect), LocationTracker, ChunkCoord, module
│   │                          capability maps, IScopedSerialization + NBTSerializationScope, Overideable,
│   │                          LazySingleSidedHolder, misc helpers
│   └── wrappers/              Vanilla/NeoForge → TechnoLich adapters and back (WrapperLevel,
│                              WrapperBlockEntity, WrapperVanillaItemStack, WrapperContainerIItemStorage,
│                              WrapperResourceHandlerIItemStorage, WrapperEnergyHandlerIBattery, ...)
├── core/                      Block entity framework: BlockEntityCore, TickableBlockEntityCore,
│   │                          EntityBlockCore, TickableEntityBlockCore, BlockEntityFragmentCollection,
│   │                          fragment interfaces, networks (INetwork, TileNetwork, NetworkManager,
│   │                          INetworkNode, TileNetworkNode), SidedStorageConfiguration
│   └── frag/                  Fragment base classes and fragments (FragColorable, FragDropInventory)
├── dev/                       Dev-only test block + game tests (never registered in production)
├── network/PacketHandler.java Thin wrapper over NeoForge's PayloadRegistrar (no payloads yet)
└── util/                      Color, InventoryUtils (item dropping), Singleton
src/test/java/...              JUnit tests + Testable* fakes that avoid vanilla objects
```

## Core concepts

### Fragments and BlockEntityCore
A `BlockEntityCore` owns a `BlockEntityFragmentCollection` (`fragList`). Behaviour is composed by adding fragments in the block entity constructor:

- `fragList.addFragment(IBlockEntityFragment<T>)`: a fragment that *exposes* a module `T` (e.g. `FragColorable` exposes itself as an `IColorable`). Its `faceToModuleMapper(be)` maps a nullable `Direction` to the current instance (or null). It is called on every query, so return live state. Expose an interface whose mutators save/sync (like `IColorable#setColor`), never a mutable value object: other mods get the same object through the capability.
- `fragList.addInternalFragment(IInternalBlockEntityFragment)`: hooks without an exposed module (serialization, `onRemove`, e.g. `FragDropInventory`).
- Fragment `name()`s must be unique per block entity (they key saved data), and each module may be exposed by only one fragment; both throw `IllegalArgumentException` otherwise.
- Each fragment gets an `IFragmentHost` through `onAttach` when added (`BlockEntityCore` is the host). Fragments extending `InternalBlockEntityFragment`/`BlockEntityFragment` call `markDirty()` after changing saved state and `markDirtyAndSync()` after changing client-visible (DESCRIPTION) state; the latter also sends a block update to clients.
- Storages report their own changes: construct `ItemStorageArray`/`ItemStorageNBT`/`PowerBattery` with an `onChanged` runnable (e.g. `this::markDirty`) and every `setSlot`/`setStorage` runs it. `setSlotQuietly`/`setStorageQuietly` skip it; the NeoForge adapters use those inside transactions and call `setChanged()` once on root commit, so aborted transactions never dirty the block entity.
- `fragList.addCapability(BlockCapability<T, Direction>, side -> T)`: expose a non-module capability, typically one of `ModuleCapabilities.STANDARD` (NeoForge item/fluid/energy).
- `fragList.addTickable(...)`: ticked by `TickableBlockEntityCore` when the block's `TickableEntityBlockCore#hasTicker(side)` returns true.

`BlockEntityCore` wires fragments into the vanilla lifecycle: `saveAdditional`/`loadAdditional` (LEVEL scope), `getUpdateTag`/`handleUpdateTag`/`onDataPacket` (DESCRIPTION scope), `setRemoved`/`clearRemoved` (fragment invalidation), and `preRemoveSideEffects` → fragment `onRemove` (server only, only when the block actually changes).

### Modules and capabilities
An `IModule<T>` is a handle for a behaviour, identified by a namespaced `Identifier` (`id()`, e.g. `technolich:colorable`), optionally backed by a NeoForge `BlockCapability<T, Direction>` and/or `ItemCapability<T, ItemAccess>`. Register with `Module.registerModule(id, blockCap[, itemCap])`; ids must be unique (the registry is thread-safe for parallel mod construction).

- Internal lookups: `IModuleProvider#getModule(module, side)` returns `Optional<T>` (implemented by `BlockEntityCore`, `IItemStack`, `WrapperBlockEntity`, etc.).
- External lookups (other mods): standard NeoForge `level.getCapability(cap, pos, side)`. For this to work, **call `ModuleCapabilities.registerBlockEntity(event, type)` for every `BlockEntityCore` type** from a `RegisterCapabilitiesEvent` listener (mod bus). It registers every module's block capability plus `STANDARD`, all routed to `BlockEntityCore#getCapability`.
- Modules must be registered before `RegisterCapabilitiesEvent`; put them in static fields of a class that is loaded during mod construction (see `Modules.init()`).
- `BlockEntityCore#deserialize` calls `invalidateCapabilities()` after loading, since fragments may replace exposed objects. If a fragment swaps its exposed object at any other time, call `invalidateCapabilities()` on the block entity.

### Scoped serialization
`IScopedSerialization` (`serializeTo(scope, ValueOutput)`, `deserialize(ValueInput, scope)`, `handlesScope(scope)`) with `NBTSerializationScope`:
- `LEVEL`: world save.
- `DESCRIPTION`: client sync (chunk load and block update packets).
- `ITEM`: data that stays with the block's item form. `BlockEntityCore#collectImplicitComponents` writes it into the `technolich:fragment_data` component (`Components.FRAGMENT_DATA`) and `applyImplicitComponents` restores it on placement. Creative pick-block (with data) carries it automatically; for survival drops the block's loot table must copy it: `{"function": "minecraft:copy_components", "source": "block_entity", "include": ["technolich:fragment_data"]}`. Keep inventories out of ITEM scope; they drop their contents instead.

Each fragment writes into its own child keyed by `name()` under the `frags` key. Use `ValueOutput`/`ValueInput` (and `Codec`s via `store`/`read`), not raw `CompoundTag`, for anything saved with a block entity.

### Items and storage
- `IItemStack` wraps `ItemStack` (`WrapperVanillaItemStack`, `IItemStack.of(stack)`, `IItemStack.Empty`). It is persisted with `IItemStack.codec()`, an `Overideable<Codec<IItemStack>>` whose default wraps `ItemStack.OPTIONAL_CODEC`. Tests override it with `TestableIItemStack.CODEC`. `components()` returns the stack's `DataComponentPatch` (items no longer carry NBT).
- `IItemStorage` is slot-based with rich default transfer logic and `ValueIOSerializable` persistence. `ItemStorageNBT` is a live view over a `CompoundTag` (e.g. inside `CUSTOM_DATA`); pass a `HolderLookup.Provider` when items may carry datapack-registry components.
- `IBattery` is a double-based energy store; `ValueIOSerializable`.

### NeoForge transfer API adapters
NeoForge 26.1 replaced `IItemHandler`/`IEnergyStorage` with transactional `ResourceHandler<ItemResource>` and `EnergyHandler`.
- TechnoLich → NeoForge: `WrapperResourceHandlerIItemStorage.of(storage)` (via `WrapperContainerIItemStorage` + NeoForge `VanillaContainerWrapper`), `new WrapperEnergyHandlerIBattery(battery)` (snapshot journal; energy is truncated to whole units). Create once per block entity and return the same instance from capability providers.
- NeoForge → TechnoLich: `ItemStorageResourceHandler`, `BatteryEnergyHandler`. Their mutators open **root** transactions, so never call them while a transaction is open.

### Networks
`INetwork`/`TileNetwork` group `INetworkNode`s (located by `Loc4`) into server-side networks managed by `NetworkManager` (`TechnoLich.NETWORK_MANAGER`, server side only; ticked from `ServerTickEvent.Pre/Post`). Nodes are found through a network module on the block entity (`TileNetwork#networkModule`). `LocationTracker` indexes locations by dimension and chunk. See the Javadoc on `INetwork` for the design rationale.

Lifecycle: `TechnoLich` forwards server `ChunkEvent.Unload` to `NetworkManager#onChunkUnload`, which drops that chunk's nodes as a batch; block entities must re-add their node when they load. Removed nodes get `onRemoved`, absorbed networks get `onTakeover`, and `addNode` moves a node out of any previous network first. Connecting two nodes pulls both (and their networks) into the network doing the connecting. Splitting explores iteratively, so long cable lines are safe.

### Engine seams for testing
`ILevel`, `IBlockEntity`, `IItemStack` and `Overideable` exist so logic can be unit tested without a running game. Tests use `TestableLevel`, `TestableIItemStack`, `TestableLoc4`, `TestableNetwork*`; `MCAssert.failVanillaClass` marks methods that must not be reached in tests.

## Dev content and game tests

`dev/` registers `technolich:dev_frag_block` (colorable, 1-slot inventory exposed via `Capabilities.Item.BLOCK`, drops on break) only when `!FMLEnvironment.isProduction()`. Place it with `/setblock ~ ~ ~ technolich:dev_frag_block` in a dev client.

`DevGameTests` registers test functions (`Registries.TEST_FUNCTION`) and test instances (`RegisterGameTestsEvent`) using vanilla's 1×1×1 `minecraft:empty` structure. Current tests: capability/module lookup, level save/load, client update tag, item capability insert with rollback, drops on break. Note `GameTestHelper#assertValueEqual(expected, actual, name)`: expected comes first.

## Testing conventions

- Unit tests: `src/test`, JUnit 5, names like `Method_ExpectedBehavior`. ModDevGradle's unit-test support puts Minecraft classes on the classpath, but anything needing registries or a level belongs in a game test.
- Add a game test for any behaviour that crosses into vanilla/NeoForge (serialization with real items, capabilities, block lifecycle).
- Verify with `./gradlew build` **and** `./gradlew runGameTestServer`.

## Code style

- Annotate reference parameters and returns with both `@NotNull`/`@Nullable` (JetBrains) and `@Nonnull` (javax), matching existing code.
- Prefer `Optional` for internal lookups; use `@Nullable T` only where NeoForge expects it (capability providers).
- Lambdas use parenthesized parameters: `(x) -> ...`.
- Keep vanilla/NeoForge types out of `api/adapters` interfaces where a TechnoLich abstraction already exists.
