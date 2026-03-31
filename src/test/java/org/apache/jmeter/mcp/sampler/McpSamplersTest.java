package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.auth.NoAuthStrategy;
import org.apache.jmeter.mcp.client.McpSessionContext;
import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpInitializeResult;
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

import java.util.Map;
import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class McpSamplersTest {
    @BeforeEach
    void setUp() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    @Test
    void initializeSamplerReturnsSuccessPayload() throws Exception {
        McpInitializeSampler sampler = new McpInitializeSampler();
        sampler.setProperty("saveResultVariable", "mcp.init");
        McpClientConfig config = config();
        McpSessionContext context = new McpSessionContext();
        context.clientKey("k1");
        McpOperations client = mock(McpOperations.class);
        when(client.initialize()).thenReturn(new McpInitializeResult("v1", JsonUtils.readTree("{}"), JsonUtils.readTree("{\"name\":\"server\"}"), "k1"));

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenReturn(config);
            mocked.when(() -> McpSamplerSupport.buildSessionContext(any())).thenReturn(context);
            mocked.when(() -> McpSamplerSupport.buildClient(config, context)).thenReturn(client);

            SampleResult result = sampler.sample(null);

            assertTrue(result.isSuccessful());
            assertTrue(result.getResponseDataAsString().contains("\"clientKey\":\"k1\""));
            assertEquals(result.getResponseDataAsString(), JMeterContextService.getContext().getVariables().get("mcp.init"));
            mocked.verify(() -> McpSamplerSupport.persistSession(sampler, context));
        }
    }

    @Test
    void initializeSamplerReturnsFailureOnException() throws Exception {
        McpInitializeSampler sampler = new McpInitializeSampler();

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenThrow(new IllegalStateException("boom"));

            SampleResult result = sampler.sample(null);

            assertFalse(result.isSuccessful());
            assertEquals("500", result.getResponseCode());
        }
    }

    @Test
    void toolsListSamplerAutoInitializesAndReturnsTools() throws Exception {
        McpToolsListSampler sampler = new McpToolsListSampler();
        sampler.setProperty("autoInitialize", "true");
        McpClientConfig config = config();
        McpSessionContext context = new McpSessionContext();
        McpOperations client = mock(McpOperations.class);
        JsonNode tools = JsonUtils.readTree("[{\"name\":\"echo\"}]");
        when(client.listTools()).thenReturn(new McpListToolsResult(tools, JsonUtils.readTree("{\"tools\":[{\"name\":\"echo\"}]}")));

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenReturn(config);
            mocked.when(() -> McpSamplerSupport.buildSessionContext(any())).thenReturn(context);
            mocked.when(() -> McpSamplerSupport.buildClient(config, context)).thenReturn(client);

            SampleResult result = sampler.sample(null);

            verify(client).initialize();
            assertTrue(result.isSuccessful());
            assertTrue(result.getResponseDataAsString().contains("\"tools\""));
        }
    }

    @Test
    void toolsCallSamplerMarksSampleFailedWhenToolReturnsError() throws Exception {
        McpToolsCallSampler sampler = new McpToolsCallSampler();
        sampler.setProperty("toolName", "echo");
        sampler.setProperty("toolArgumentsJson", "{\"msg\":\"hi\"}");
        McpClientConfig config = config();
        McpSessionContext context = new McpSessionContext();
        context.negotiatedProtocolVersion("v1");
        context.clientKey("k1");
        McpOperations client = mock(McpOperations.class);
        when(client.callTool(eq("echo"), eq(Map.of("msg", "hi")), any())).thenReturn(
                new McpToolCallResult(JsonUtils.readTree("[]"), JsonUtils.readTree("{\"ok\":false}"), true, JsonUtils.readTree("{\"isError\":true}"))
        );

        try (MockedStatic<McpSamplerSupport> mocked = mockStatic(McpSamplerSupport.class)) {
            mocked.when(() -> McpSamplerSupport.buildConfig(any())).thenReturn(config);
            mocked.when(() -> McpSamplerSupport.buildSessionContext(any())).thenReturn(context);
            mocked.when(() -> McpSamplerSupport.buildClient(config, context)).thenReturn(client);
            mocked.when(() -> McpSamplerSupport.parseArguments("{\"msg\":\"hi\"}")).thenReturn(Map.of("msg", "hi"));

            SampleResult result = sampler.sample(null);

            assertFalse(result.isSuccessful());
            assertTrue(result.getResponseMessage().contains("isError=true"));
            assertTrue(result.getResponseDataAsString().contains("\"toolName\":\"echo\""));
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
}
