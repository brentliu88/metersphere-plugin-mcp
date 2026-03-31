package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.model.McpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class McpClientRegistry {
    private static final Logger logger = LoggerFactory.getLogger(McpClientRegistry.class);
    private static final ConcurrentMap<String, ClientHolder> CLIENTS = new ConcurrentHashMap<>();

    private McpClientRegistry() {
    }

    public static McpOperations getOrCreate(McpClientConfig config, McpSessionContext sessionContext) {
        String requestedKey = sessionContext.clientKey();
        String clientKey = (requestedKey == null || requestedKey.isBlank())
                ? "mcp-sdk-" + UUID.randomUUID()
                : requestedKey;
        sessionContext.clientKey(clientKey);

        ClientHolder holder = CLIENTS.compute(clientKey, (key, existing) -> {
            if (existing == null) {
                logger.info("Creating MCP SDK client. clientKey={}, endpoint={}", key, config.endpoint());
                return new ClientHolder(config.fingerprint(), SdkMcpClientFactory.create(config, sessionContext));
            }
            if (!existing.configFingerprint().equals(config.fingerprint())) {
                logger.error("Rejected MCP client reuse due to config mismatch. clientKey={}, endpoint={}", key, config.endpoint());
                throw new IllegalStateException("MCP clientKey '" + key + "' is already bound to a different connection configuration.");
            }
            logger.info("Reusing MCP SDK client. clientKey={}, endpoint={}", key, config.endpoint());
            existing.client().adopt(sessionContext);
            return existing;
        });
        holder.client().adopt(sessionContext);
        return holder.client();
    }

    private record ClientHolder(String configFingerprint, McpOperations client) {
    }
}
