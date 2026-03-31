package org.apache.jmeter.mcp;

import io.metersphere.plugin.core.ui.PluginResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiScriptApiImplTest {
    @Test
    void initLoadsThreeUiScripts() {
        UiScriptApiImpl api = new UiScriptApiImpl();

        PluginResource resource = api.init();

        assertEquals("mcp-v0.1.0", resource.getPluginId());
        assertEquals(3, resource.getUiScripts().size());
        assertTrue(api.xpack());
    }

    @Test
    void customMethodReturnsNull() {
        UiScriptApiImpl api = new UiScriptApiImpl();

        assertNull(api.customMethod("request"));
    }
}
