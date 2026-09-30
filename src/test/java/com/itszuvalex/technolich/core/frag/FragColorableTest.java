package com.itszuvalex.technolich.core.frag;

import com.itszuvalex.technolich.core.TestableFragmentHost;
import com.itszuvalex.technolich.util.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FragColorableTest {
    @Test
    void SetColor_Changed_MarksDirtyAndSyncs() {
        var host = new TestableFragmentHost();
        var frag = new FragColorable();
        frag.onAttach(host);

        frag.setColor(new Color(0xFF00FF00));

        Assertions.assertEquals(new Color(0xFF00FF00), frag.getColor());
        Assertions.assertEquals(1, host.syncCount);
    }

    @Test
    void SetColor_Unchanged_DoesNothing() {
        var host = new TestableFragmentHost();
        var frag = new FragColorable(new Color(0xFF00FF00));
        frag.onAttach(host);

        frag.setColor(new Color(0xFF00FF00));

        Assertions.assertEquals(0, host.dirtyCount);
    }
}
