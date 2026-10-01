package com.itszuvalex.technolich.api.storage

import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.common.util.ValueIOSerializable
import kotlin.math.max
import kotlin.math.min

/**
 * A double-valued energy store.
 */
interface IBattery : ValueIOSerializable {
    /**
     * The stored energy. Setting it notifies batteries with a change listener (see [setChanged]).
     */
    var storage: Double

    /**
     * Sets the stored energy without notifying any change listener, e.g. inside a NeoForge transaction.
     */
    fun setStorageQuietly(storage: Double) {
        this.storage = storage
    }

    val maxStorage: Double

    val room: Double get() = maxStorage - storage

    /**
     * @param amt Energy to add; negative amounts are treated as 0.
     * @return Energy actually added.
     */
    fun fill(amt: Double): Double {
        val toFill = max(0.0, min(amt, room))
        if (toFill > 0) storage += toFill
        return toFill
    }

    /**
     * @param amt Energy to remove; negative amounts are treated as 0.
     * @return Energy actually removed.
     */
    fun drain(amt: Double): Double {
        val toDrain = max(0.0, min(amt, storage))
        if (toDrain > 0) storage -= toDrain
        return toDrain
    }

    /**
     * Notifies the battery's change listener, if any (e.g. the owning block entity's setChanged).
     */
    fun setChanged() {}

    companion object {
        @JvmField
        val Empty: IBattery = object : IBattery {
            override var storage: Double
                get() = 0.0
                set(_) {}
            override val maxStorage: Double get() = 0.0
            override fun serialize(output: ValueOutput) {}
            override fun deserialize(input: ValueInput) {}
        }
    }
}
