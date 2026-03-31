package org.apache.jmeter.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;

public record McpListToolsResult(
        JsonNode tools,
        JsonNode rawResult
) {
}
