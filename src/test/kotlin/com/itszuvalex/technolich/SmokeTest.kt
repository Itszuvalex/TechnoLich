package com.itszuvalex.technolich

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SmokeTest {
    @Test
    fun modId_IsLowercase() {
        assertEquals(TechnoLich.ID.lowercase(), TechnoLich.ID)
    }
}
