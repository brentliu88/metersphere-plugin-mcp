package org.apache.jmeter.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;

public record McpToolCallResult(
        JsonNode content,
        JsonNode structuredContent,
        Boolean isError,
        JsonNode rawResult
) {
}
