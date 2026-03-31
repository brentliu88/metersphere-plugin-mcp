package org.apache.jmeter.mcp.runtime;

import org.apache.jmeter.mcp.auth.ApiKeyAuthStrategy;
import org.apache.jmeter.mcp.auth.BearerTokenAuthStrategy;
import org.apache.jmeter.mcp.auth.CustomHeadersAuthStrategy;
import org.apache.jmeter.mcp.auth.NoAuthStrategy;
import org.apache.jmeter.mcp.client.McpSessionContext;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpSamplerSupportTest {
    private TestSampler sampler;

    @BeforeEach
    void setUp() {
        sampler = new TestSampler();
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        sampler.setProperty("baseUrl", "https://example.com/mcp");
    }

    @Test
    void buildConfigUsesDefaultsAndNoAuth() throws Exception {
        McpClientConfig config = McpSamplerSupport.buildConfig(sampler);

        assertEquals("https://example.com/mcp", config.endpoint().toString());
        assertEquals(5000L, config.connectTimeout().toMillis());
        assertEquals(30000L, config.requestTimeout().toMillis());
        assertInstanceOf(NoAuthStrategy.class, config.authStrategy());
    }

    @Test
    void buildConfigCreatesBearerStrategy() throws Exception {
        sampler.setProperty("authorizationType", "bearer");
        sampler.setProperty("bearerToken", "abc");

        McpClientConfig config = McpSamplerSupport.buildConfig(sampler);

        assertInstanceOf(BearerTokenAuthStrategy.class, config.authStrategy());
        assertEquals(Map.of("Authorization", "Bearer abc"), config.authStrategy().headers());
    }

    @Test
    void buildConfigCreatesApiKeyStrategy() throws Exception {
        sampler.setProperty("authorizationType", "apikey");
        sampler.setProperty("apiKeyHeaderName", "X-TOKEN");
        sampler.setProperty("apiKeyValue", "v1");

        McpClientConfig config = McpSamplerSupport.buildConfig(sampler);

        assertInstanceOf(ApiKeyAuthStrategy.class, config.authStrategy());
        assertEquals(Map.of("X-TOKEN", "v1"), config.authStrategy().headers());
    }

    @Test
    void buildConfigCreatesCustomHeadersStrategy() throws Exception {
        sampler.setProperty("authorizationType", "headers");
        sampler.setProperty("customHeadersJson", "{\"A\":\"1\"}");

        McpClientConfig config = McpSamplerSupport.buildConfig(sampler);

        assertInstanceOf(CustomHeadersAuthStrategy.class, config.authStrategy());
        assertEquals(Map.of("A", "1"), config.authStrategy().headers());
    }

    @Test
    void buildConfigRejectsUnsupportedAuthType() {
        sampler.setProperty("authorizationType", "unsupported");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> McpSamplerSupport.buildConfig(sampler));

        assertTrue(ex.getMessage().contains("Unsupported authorizationType"));
    }

    @Test
    void buildSessionContextReadsClientKeyAndProtocolVersionFromSamplerAndVars() {
        sampler.setProperty("clientKey", "explicit-key");
        sampler.callSetVar(McpSamplerSupport.VAR_PROTOCOL_VERSION, "2025-03-26");

        McpSessionContext context = McpSamplerSupport.buildSessionContext(sampler);

        assertEquals("explicit-key", context.clientKey());
        assertEquals("2025-03-26", context.negotiatedProtocolVersion());
    }

    @Test
    void buildSessionContextFallsBackToVariables() {
        sampler.callSetVar(McpSamplerSupport.VAR_CLIENT_KEY, "from-var");
        sampler.callSetVar(McpSamplerSupport.VAR_PROTOCOL_VERSION, "from-version");

        McpSessionContext context = McpSamplerSupport.buildSessionContext(sampler);

        assertEquals("from-var", context.clientKey());
        assertEquals("from-version", context.negotiatedProtocolVersion());
    }

    @Test
    void persistSessionStoresVariables() {
        McpSessionContext context = new McpSessionContext();
        context.clientKey("k1");
        context.negotiatedProtocolVersion("v1");

        McpSamplerSupport.persistSession(sampler, context);

        assertEquals("k1", sampler.callGetVar(McpSamplerSupport.VAR_CLIENT_KEY));
        assertEquals("v1", sampler.callGetVar(McpSamplerSupport.VAR_PROTOCOL_VERSION));
    }

    @Test
    void parseArgumentsHandlesBlankAndJson() throws Exception {
        assertTrue(McpSamplerSupport.parseArguments("").isEmpty());
        assertEquals(Map.of("a", 1), McpSamplerSupport.parseArguments("{\"a\":1}"));
    }

    private static final class TestSampler extends McpSamplerBase {
        @Override
        public SampleResult sample(org.apache.jmeter.samplers.Entry e) {
            return null;
        }

        void callSetVar(String name, String value) { setVar(name, value); }
        String callGetVar(String name) { return getVar(name); }
    }
}
