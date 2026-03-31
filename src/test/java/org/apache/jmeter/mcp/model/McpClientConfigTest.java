package org.apache.jmeter.mcp.model;

import org.apache.jmeter.mcp.auth.NoAuthStrategy;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpClientConfigTest {
    @Test
    void defaultsAuthStrategyToNoAuthWhenNull() {
        McpClientConfig config = new McpClientConfig(
                URI.create("https://example.com/mcp"),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2),
                null
        );

        assertInstanceOf(NoAuthStrategy.class, config.authStrategy());
    }

    @Test
    void fingerprintIncludesEndpointTimeoutsAndHeaders() {
        McpClientConfig config = new McpClientConfig(
                URI.create("https://example.com/mcp"),
                Duration.ofMillis(1000),
                Duration.ofMillis(2000),
                new NoAuthStrategy()
        );

        String fingerprint = config.fingerprint();

        assertTrue(fingerprint.contains("https://example.com/mcp"));
        assertTrue(fingerprint.contains("1000"));
        assertTrue(fingerprint.contains("2000"));
        assertEquals(URI.create("https://example.com/mcp"), config.endpoint());
    }
}
