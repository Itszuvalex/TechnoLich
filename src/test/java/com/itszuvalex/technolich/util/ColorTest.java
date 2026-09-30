package com.itszuvalex.technolich.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ColorTest {
    @Test
    void IntRoundTrip_PreservesAllChannels() {
        for (int argb : new int[]{0, 0xFF102030, 0x80FFFFFF, 0x7F000001, -1}) {
            var color = new Color(argb);
            Assertions.assertEquals(argb, color.toInt());
        }
        var c = new Color(0xFF102030);
        Assertions.assertEquals((byte) 0xFF, c.alpha());
        Assertions.assertEquals((byte) 0x10, c.red());
        Assertions.assertEquals((byte) 0x20, c.green());
        Assertions.assertEquals((byte) 0x30, c.blue());
    }

    @Test
    void With_ReturnsChangedCopyLeavesOriginal() {
        var original = new Color(0x01020304);
        var changed = original.withRed((byte) 9);
        Assertions.assertEquals(0x01020304, original.toInt());
        Assertions.assertEquals(0x01090304, changed.toInt());
        Assertions.assertEquals(new Color(0x01090304), changed);
    }
}
