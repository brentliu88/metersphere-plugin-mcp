package org.apache.jmeter.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;

public record McpInitializeResult(
        String protocolVersion,
        JsonNode capabilities,
        JsonNode serverInfo,
        String clientKey
) {
}
