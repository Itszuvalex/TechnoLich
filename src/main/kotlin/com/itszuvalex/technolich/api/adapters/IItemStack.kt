package com.itszuvalex.technolich.api.adapters

import com.itszuvalex.technolich.api.utility.MCConstants
import com.itszuvalex.technolich.api.utility.Overideable
import com.itszuvalex.technolich.api.wrappers.WrapperVanillaItemStack
import com.mojang.serialization.Codec
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

/**
 * Engine seam over [ItemStack], so storage logic can be unit tested without vanilla items.
 */
interface IItemStack : IModuleProvider {
    val item: Identifier

    var stackSize: Int

    /**
     * THIS IS NOT VALIDATED. USE ONLY WHEN YOU KNOW `change` IS SAFE.
     *
     * @param change Amount to modify (+1, -1).
     */
    fun modifyStackSize(change: Int) {
        stackSize += change
    }

    val stackSizeMax: Int

    var damage: Int

    val damageMax: Int

    /**
     * Data components that differ from the item's defaults.
     */
    val components: DataComponentPatch

    val hasComponents: Boolean get() = !components.isEmpty

    fun toMinecraft(): ItemStack

    val isEmpty: Boolean

    val room: Int get() = stackSizeMax - stackSize

    fun copy(): IItemStack

    fun isItemEqual(other: IItemStack): Boolean

    companion object {
        @JvmField
        val Empty: IItemStack = object : IItemStack {
            override fun <T : Any> getModule(module: IModule<T>, side: Direction?): T? = null
            override val item: Identifier get() = BuiltInRegistries.ITEM.defaultKey
            override var stackSize: Int
                get() = 0
                set(_) {}
            override val stackSizeMax: Int get() = MCConstants.ITEMSTACK_MAX
            override var damage: Int
                get() = 0
                set(_) {}
            override val damageMax: Int get() = 0
            override val components: DataComponentPatch get() = DataComponentPatch.EMPTY
            override val hasComponents: Boolean get() = false
            override fun toMinecraft(): ItemStack = ItemStack.EMPTY
            override val isEmpty: Boolean get() = true
            override val room: Int get() = 0
            override fun copy(): IItemStack = this
            override fun isItemEqual(other: IItemStack): Boolean = other.isEmpty
            override fun toString(): String = "IItemStack.Empty"
        }

        /**
         * Codec used to persist IItemStacks. Overridable so tests can serialize without vanilla ItemStacks.
         */
        @JvmField
        val CODEC: Overideable<Codec<IItemStack>> =
            Overideable(ItemStack.OPTIONAL_CODEC.xmap({ of(it) }, { it.toMinecraft() }))

        @JvmStatic
        fun codec(): Codec<IItemStack> = CODEC.get()

        @JvmStatic
        fun of(stack: ItemStack): IItemStack = if (stack.isEmpty) Empty else WrapperVanillaItemStack(stack)
    }
}
