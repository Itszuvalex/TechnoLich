package com.itszuvalex.technolich.api.adapters

import com.itszuvalex.technolich.util.Color

/**
 * Something with a color that can be read and changed, e.g. through the
 * [com.itszuvalex.technolich.api.Capabilities.COLORABLE] capability.
 */
interface IColorable {
    /**
     * Setting it persists and syncs the change as needed.
     */
    var color: Color
}
