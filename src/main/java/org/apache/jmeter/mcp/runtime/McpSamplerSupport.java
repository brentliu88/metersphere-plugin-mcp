package org.apache.jmeter.mcp.runtime;

import org.apache.jmeter.mcp.auth.ApiKeyAuthStrategy;
import org.apache.jmeter.mcp.auth.AuthStrategy;
import org.apache.jmeter.mcp.auth.BearerTokenAuthStrategy;
import org.apache.jmeter.mcp.auth.CustomHeadersAuthStrategy;
import org.apache.jmeter.mcp.auth.NoAuthStrategy;
import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.client.SdkMcpClientFactory;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public final class McpSamplerSupport {
    private static final Logger logger = LoggerFactory.getLogger(McpSamplerSupport.class);

    private McpSamplerSupport() {
    }

    public static McpClientConfig buildConfig(McpSamplerBase sampler) throws Exception {
        McpClientConfig config = new McpClientConfig(
                URI.create(sampler.getResolvedPropertyOrDefault("baseUrl", "")),
                Duration.ofMillis(sampler.getLongProperty("connectTimeoutMs", 5000L)),
                Duration.ofMillis(sampler.getLongProperty("requestTimeoutMs", 30000L)),
                buildAuthStrategy(sampler)
        );
        logger.info("Built MCP client config. endpoint={}, connectTimeoutMs={}, requestTimeoutMs={}",
                config.endpoint(), config.connectTimeout().toMillis(), config.requestTimeout().toMillis());
        return config;
    }

    public static McpOperations buildClient(McpClientConfig config) {
        logger.info("Creating stateless MCP client. endpoint={}", config.endpoint());
        return SdkMcpClientFactory.create(config);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseArguments(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, Object> value = JsonUtils.MAPPER.readValue(json, Map.class);
        return value == null ? Collections.emptyMap() : value;
    }

    @SuppressWarnings("unchecked")
    private static AuthStrategy buildAuthStrategy(McpSamplerBase sampler) throws Exception {
        String authType = sampler.getResolvedPropertyOrDefault("authorizationType", "none").trim().toLowerCase();
        return switch (authType) {
            case "bearer" -> new BearerTokenAuthStrategy(sampler.getResolvedProperty("bearerToken"));
            case "apikey" -> new ApiKeyAuthStrategy(
                    sampler.getResolvedPropertyOrDefault("apiKeyHeaderName", "X-API-Key"),
                    sampler.getResolvedProperty("apiKeyValue")
            );
            case "headers" -> {
                Map<String, String> headers = JsonUtils.MAPPER.readValue(
                        sampler.getResolvedPropertyOrDefault("customHeadersJson", "{}"),
                        Map.class
                );
                yield new CustomHeadersAuthStrategy(headers == null ? Collections.emptyMap() : headers);
            }
            case "none", "" -> new NoAuthStrategy();
            default -> throw new IllegalArgumentException("Unsupported authorizationType: " + authType);
        };
    }
}
