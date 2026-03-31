package org.apache.jmeter.mcp.auth;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthStrategiesTest {
    @Test
    void bearerTokenStrategyReturnsAuthorizationHeader() {
        BearerTokenAuthStrategy strategy = new BearerTokenAuthStrategy("token-1");

        assertEquals(Map.of("Authorization", "Bearer token-1"), strategy.headers());
    }

    @Test
    void apiKeyStrategyReturnsConfiguredHeader() {
        ApiKeyAuthStrategy strategy = new ApiKeyAuthStrategy("X-API-Key", "secret");

        assertEquals(Map.of("X-API-Key", "secret"), strategy.headers());
    }

    @Test
    void customHeadersStrategyReturnsProvidedHeaders() {
        Map<String, String> headers = Map.of("A", "1", "B", "2");
        CustomHeadersAuthStrategy strategy = new CustomHeadersAuthStrategy(headers);

        assertEquals(headers, strategy.headers());
    }

    @Test
    void noAuthStrategyReturnsEmptyHeaders() {
        NoAuthStrategy strategy = new NoAuthStrategy();

        assertTrue(strategy.headers().isEmpty());
    }
}
