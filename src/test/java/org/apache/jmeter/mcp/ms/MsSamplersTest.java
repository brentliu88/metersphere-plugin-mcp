package org.apache.jmeter.mcp.ms;

import io.metersphere.plugin.core.MsTestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.mcp.sampler.McpToolsCallSampler;
import org.apache.jmeter.mcp.sampler.McpToolsListSampler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MsSamplersTest {
    @Test
    void samplerBasePropertiesRoundTrip() {
        TestMsSamplerBase base = new TestMsSamplerBase();

        base.setComments("c");
        base.setBaseUrl("https://example.com/mcp");
        base.setConnectTimeoutMs("100");
        base.setRequestTimeoutMs("200");
        base.setAuthorizationType("bearer");
        base.setBearerToken("token");
        base.setApiKeyHeaderName("X-Key");
        base.setApiKeyValue("value");
        base.setCustomHeadersJson("{\"A\":\"1\"}");
        base.setSaveResultVariable("save");

        assertEquals("c", base.getComments());
        assertEquals("https://example.com/mcp", base.getBaseUrl());
        assertEquals("100", base.getConnectTimeoutMs());
        assertEquals("200", base.getRequestTimeoutMs());
        assertEquals("bearer", base.getAuthorizationType());
        assertEquals("token", base.getBearerToken());
        assertEquals("X-Key", base.getApiKeyHeaderName());
        assertEquals("value", base.getApiKeyValue());
        assertEquals("{\"A\":\"1\"}", base.getCustomHeadersJson());
        assertEquals("save", base.getSaveResultVariable());
    }

    @Test
    void toolsListAndCallSamplersPopulateSpecificProperties() {
        MsMcpToolsListSampler list = new MsMcpToolsListSampler();
        list.setEnable(true);
        list.setName("List");
        list.setBaseUrl("https://example.com/mcp");
        list.setConnectTimeoutMs("123");
        list.setRequestTimeoutMs("456");
        McpToolsListSampler listSampler = list.initSampler(null);

        MsMcpToolsCallSampler call = new MsMcpToolsCallSampler();
        call.setEnable(true);
        call.setName("Call");
        call.setBaseUrl("https://example.com/mcp");
        call.setToolName("echo");
        call.setToolArgumentsJson("{\"a\":1}");
        call.setClazzName("custom.Call");
        McpToolsCallSampler callSampler = call.initSampler(null);

        assertEquals("List", listSampler.getName());
        assertNotNull(list.getClazzName());
        assertEquals("123", listSampler.getPropertyAsString("connectTimeoutMs"));
        assertEquals("custom.Call", call.getClazzName());
        assertEquals("echo", call.getToolName());
        assertEquals("{\"a\":1}", call.getToolArgumentsJson());
        assertEquals("echo", callSampler.getPropertyAsString("toolName"));
        assertEquals("{\"a\":1}", callSampler.getPropertyAsString("toolArgumentsJson"));
    }

    @Test
    void toHashTreeAddsSamplerWhenEnabledAndSkipsWhenDisabled() {
        HashTree root = new HashTree();
        MsMcpToolsListSampler enabled = new MsMcpToolsListSampler();
        enabled.setEnable(true);
        enabled.setName("Enabled");

        MsTestElement child = new MsTestElement() {
            @Override
            public void toHashTree(HashTree tree, List<MsTestElement> hashTree, io.metersphere.plugin.core.MsParameter config) {
                tree.add("child");
            }
        };

        enabled.toHashTree(root, List.of(child), null);
        assertEquals(1, root.size());

        MsMcpToolsListSampler disabled = new MsMcpToolsListSampler();
        disabled.setEnable(false);
        disabled.toHashTree(new HashTree(), List.of(), null);
        assertNotNull(disabled.getClazzName());
    }

    private static final class TestMsSamplerBase extends MsMcpSamplerBase {
        @Override
        public String getClazzName() {
            return "test";
        }
    }
}
