package org.apache.jmeter.mcp.auth;

import java.util.Map;
import java.util.Objects;

public final class CustomHeadersAuthStrategy implements AuthStrategy {
    private final Map<String, String> headers;

    public CustomHeadersAuthStrategy(Map<String, String> headers) {
        this.headers = Objects.requireNonNull(headers, "headers must not be null");
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }
}
