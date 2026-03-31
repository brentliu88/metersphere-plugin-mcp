package org.apache.jmeter.mcp.auth;

import java.util.Map;
import java.util.Objects;

public final class BearerTokenAuthStrategy implements AuthStrategy {
    private final String token;

    public BearerTokenAuthStrategy(String token) {
        this.token = Objects.requireNonNull(token, "token must not be null");
    }

    @Override
    public Map<String, String> headers() {
        return Map.of("Authorization", "Bearer " + token);
    }
}
