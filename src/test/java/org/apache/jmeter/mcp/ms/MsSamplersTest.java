package org.apache.jmeter.mcp.ms;

import io.metersphere.plugin.core.MsTestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.mcp.sampler.McpInitializeSampler;
import org.apache.jmeter.mcp.sampler.McpToolsCallSampler;
import org.apache.jmeter.mcp.sampler.McpToolsListSampler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MsSamplersTest {
    @Test
    void initializeSamplerCopiesCommonProperties() {
        MsMcpInitializeSampler ms = new MsMcpInitializeSampler();
        ms.setEnable(true);
        ms.setName("Init");
        ms.setBaseUrl("https://example.com/mcp");
        ms.setAuthorizationType("none");
        ms.setConnectTimeoutMs("123");
        ms.setRequestTimeoutMs("456");
        ms.setClientKey("k1");
        ms.setSaveResultVariable("saveVar");

        McpInitializeSampler sampler = ms.initSampler(null);

        assertEquals("Init", sampler.getName());
        assertEquals("https://example.com/mcp", sampler.getPropertyAsString("baseUrl"));
        assertEquals("123", sampler.getPropertyAsString("connectTimeoutMs"));
        assertEquals("456", sampler.getPropertyAsString("requestTimeoutMs"));
        assertEquals("k1", sampler.getPropertyAsString("clientKey"));
        assertEquals("saveVar", sampler.getPropertyAsString("saveResultVariable"));
    }

    @Test
    void toolsListAndCallSamplersPopulateSpecificProperties() {
        MsMcpToolsListSampler list = new MsMcpToolsListSampler();
        list.setEnable(true);
        list.setName("List");
        list.setBaseUrl("https://example.com/mcp");
        McpToolsListSampler listSampler = list.initSampler(null);

        MsMcpToolsCallSampler call = new MsMcpToolsCallSampler();
        call.setEnable(true);
        call.setName("Call");
        call.setBaseUrl("https://example.com/mcp");
        call.setToolName("echo");
        call.setToolArgumentsJson("{\"a\":1}");
        McpToolsCallSampler callSampler = call.initSampler(null);

        assertEquals("List", listSampler.getName());
        assertEquals("echo", callSampler.getPropertyAsString("toolName"));
        assertEquals("{\"a\":1}", callSampler.getPropertyAsString("toolArgumentsJson"));
    }

    @Test
    void toHashTreeAddsSamplerWhenEnabledAndSkipsWhenDisabled() {
        HashTree root = new HashTree();
        MsMcpInitializeSampler enabled = new MsMcpInitializeSampler();
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

        MsMcpInitializeSampler disabled = new MsMcpInitializeSampler();
        disabled.setEnable(false);
        disabled.toHashTree(new HashTree(), List.of(), null);
        assertNotNull(disabled.getClazzName());
    }
}
