package com.itszuvalex.technolich.api.adapters

import com.itszuvalex.technolich.util.Color

/**
 * Something with a color that can be read and changed, e.g. through the
 * [com.itszuvalex.technolich.api.Capabilities.COLORABLE] capability.
 */
interface IColorable {
    fun getColor(): Color

    /**
     * Implementations persist and sync the change as needed.
     */
    fun setColor(color: Color)
}
