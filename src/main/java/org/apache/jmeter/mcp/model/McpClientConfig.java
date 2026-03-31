package org.apache.jmeter.mcp.model;

import org.apache.jmeter.mcp.auth.AuthStrategy;
import org.apache.jmeter.mcp.auth.NoAuthStrategy;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public final class McpClientConfig {
    private final URI endpoint;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final AuthStrategy authStrategy;

    public McpClientConfig(URI endpoint,
                           Duration connectTimeout,
                           Duration requestTimeout,
                           AuthStrategy authStrategy) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
        this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout must not be null");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
        this.authStrategy = authStrategy == null ? new NoAuthStrategy() : authStrategy;
    }

    public URI endpoint() { return endpoint; }
    public Duration connectTimeout() { return connectTimeout; }
    public Duration requestTimeout() { return requestTimeout; }
    public AuthStrategy authStrategy() { return authStrategy; }

    public String fingerprint() {
        return endpoint + "|" + connectTimeout.toMillis() + "|" + requestTimeout.toMillis() + "|" + authStrategy.headers();
    }
}
