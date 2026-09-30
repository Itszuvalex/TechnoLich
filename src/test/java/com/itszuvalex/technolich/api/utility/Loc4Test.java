package com.itszuvalex.technolich.api.utility;

import com.itszuvalex.technolich.TestableLevel;
import com.itszuvalex.technolich.TestableLoc4;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class Loc4Test {
    @Test
    void Equals_DifferentSubclassesSameLocation_EqualWithSameHash() {
        var pos = new BlockPos(1, 2, 3);
        var level = new TestableLevel(TestableLoc4.DEFAULT_DIM);
        Loc4 anchored = Loc4.of(level, pos);
        Loc4 indirect = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, pos);

        Assertions.assertEquals(anchored, indirect);
        Assertions.assertEquals(indirect, anchored);
        Assertions.assertEquals(anchored.hashCode(), indirect.hashCode());
        Assertions.assertEquals(0, anchored.compareTo(indirect));
    }

    @Test
    void Equals_DifferentSubclassesSameLocation_InterchangeableAsMapKeys() {
        var pos = new BlockPos(1, 2, 3);
        var map = new HashMap<Loc4, String>();
        map.put(Loc4.of(new TestableLevel(TestableLoc4.DEFAULT_DIM), pos), "node");

        Assertions.assertEquals("node", map.get(new Loc4Indirect(TestableLoc4.DEFAULT_DIM, pos)));
    }

    @Test
    void Equals_DifferentDimensionOrPosition_NotEqual() {
        var pos = new BlockPos(1, 2, 3);
        var loc = new Loc4Indirect(TestableLoc4.DEFAULT_DIM, pos);

        Assertions.assertNotEquals(loc, new Loc4Indirect(Identifier.parse("other"), pos));
        Assertions.assertNotEquals(loc, new Loc4Indirect(TestableLoc4.DEFAULT_DIM, pos.above()));
        Assertions.assertNotEquals(loc, pos);
    }
}
