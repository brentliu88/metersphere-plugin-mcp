package org.apache.jmeter.mcp;

import io.metersphere.plugin.core.ui.PluginResource;
import io.metersphere.plugin.core.ui.UiScript;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiScriptApiImplTest {
    @Test
    void initLoadsTwoUiScripts() {
        UiScriptApiImpl api = new UiScriptApiImpl();

        PluginResource resource = api.init();

        assertEquals("mcp-v0.1.0", resource.getPluginId());
        assertEquals(2, resource.getUiScripts().size());
        List<UiScript> scripts = resource.getUiScripts();
        assertEquals("mcp_tools_list", scripts.get(0).getId());
        assertEquals("MCP Tools List", scripts.get(0).getName());
        assertEquals("mcp_tools_call", scripts.get(1).getId());
        assertEquals("MCP Tools Call", scripts.get(1).getName());
        assertTrue(api.xpack());
    }

    @Test
    void customMethodReturnsNull() {
        UiScriptApiImpl api = new UiScriptApiImpl();

        assertNull(api.customMethod("request"));
    }
}
