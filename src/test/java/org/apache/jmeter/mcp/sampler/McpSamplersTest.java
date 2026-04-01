package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.auth.NoAuthStrategy;
import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import org.apache.jmeter.mcp.runtime.McpSamplerSupport;
import org.apache.jmeter.mcp.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class McpSamplersTest {
    @BeforeEach
    void setUp() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    @Test
    void toolsListSamplerReturnsSuccessPayload() throws Exception {
        McpToolsListSampler sampler = new McpToolsListSampler();
        sampler.setProperty("saveResultVariable", "mcp.tools");
        McpClientConfig config = config();
        JsonNode tools = JsonUtils.readTree("[{\"name\":\"echo\"}]");
        FakeOperations client = new FakeOperations(
                "v1",
                new McpListToolsResult(tools, JsonUtils.readTree("{\"tools\":[{\"name\":\"echo\"}]}")),
                null
        );

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenReturn(config);
            mocked.when(() -> McpSamplerSupport.buildClient(config)).thenReturn(client);

            SampleResult result = sampler.sample(null);

            assertTrue(result.isSuccessful());
            assertTrue(result.getResponseDataAsString().contains("\"protocolVersion\":\"v1\""));
            assertEquals(result.getResponseDataAsString(), JMeterContextService.getContext().getVariables().get("mcp.tools"));
            assertTrue(client.closed);
        }
    }

    @Test
    void toolsListSamplerReturnsFailureOnException() throws Exception {
        McpToolsListSampler sampler = new McpToolsListSampler();

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenThrow(new IllegalStateException("boom"));

            SampleResult result = sampler.sample(null);

            assertFalse(result.isSuccessful());
            assertEquals("500", result.getResponseCode());
        }
    }

    @Test
    void toolsCallSamplerMarksSampleFailedWhenToolReturnsError() throws Exception {
        McpToolsCallSampler sampler = new McpToolsCallSampler();
        sampler.setProperty("toolName", "echo");
        sampler.setProperty("toolArgumentsJson", "{\"msg\":\"hi\"}");
        McpClientConfig config = config();
        FakeOperations client = new FakeOperations(
                "v1",
                null,
                new McpToolCallResult(JsonUtils.readTree("[]"), JsonUtils.readTree("{\"ok\":false}"), true, JsonUtils.readTree("{\"isError\":true}"))
        );

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenReturn(config);
            mocked.when(() -> McpSamplerSupport.buildClient(config)).thenReturn(client);
            mocked.when(() -> McpSamplerSupport.parseArguments("{\"msg\":\"hi\"}")).thenReturn(Map.of("msg", "hi"));

            SampleResult result = sampler.sample(null);

            assertFalse(result.isSuccessful());
            assertTrue(result.getResponseMessage().contains("isError=true"));
            assertTrue(result.getResponseDataAsString().contains("\"toolName\":\"echo\""));
            assertEquals("echo", client.lastToolName);
            assertEquals(Map.of("msg", "hi"), client.lastArguments);
            assertTrue(client.closed);
        }
    }

    private static McpClientConfig config() {
        return new McpClientConfig(
                URI.create("https://example.com/mcp"),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2),
                new NoAuthStrategy()
        );
    }

    private static final class FakeOperations implements McpOperations {
        private final String protocolVersion;
        private final McpListToolsResult listToolsResult;
        private final McpToolCallResult callToolResult;
        private boolean closed;
        private String lastToolName;
        private Map<String, Object> lastArguments;

        private FakeOperations(String protocolVersion, McpListToolsResult listToolsResult, McpToolCallResult callToolResult) {
            this.protocolVersion = protocolVersion;
            this.listToolsResult = listToolsResult;
            this.callToolResult = callToolResult;
        }

        @Override
        public String protocolVersion() {
            return protocolVersion;
        }

        @Override
        public McpListToolsResult listTools() {
            return listToolsResult;
        }

        @Override
        public McpToolCallResult callTool(String toolName, Map<String, Object> arguments, org.apache.jmeter.mcp.client.ProgressListener progressListener) {
            this.lastToolName = toolName;
            this.lastArguments = arguments;
            if (progressListener != null) {
                progressListener.onProgress("Handled by MCP Java SDK client", null, null);
            }
            return callToolResult;
        }

        @Override
        public void closeSession() {
            closed = true;
        }
    }
}
