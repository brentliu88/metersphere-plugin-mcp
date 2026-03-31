package org.apache.jmeter.mcp.auth;

import java.util.Map;
import java.util.Objects;

public final class ApiKeyAuthStrategy implements AuthStrategy {
    private final String headerName;
    private final String headerValue;

    public ApiKeyAuthStrategy(String headerName, String headerValue) {
        this.headerName = Objects.requireNonNull(headerName, "headerName must not be null");
        this.headerValue = Objects.requireNonNull(headerValue, "headerValue must not be null");
    }

    @Override
    public Map<String, String> headers() {
        return Map.of(headerName, headerValue);
    }
}
