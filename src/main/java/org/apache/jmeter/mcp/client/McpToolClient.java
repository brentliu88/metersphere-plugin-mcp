package org.apache.jmeter.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class McpToolClient implements McpOperations {
    private static final Logger logger = LoggerFactory.getLogger(McpToolClient.class);

    private final SdkSyncClientDelegate delegate;
    private volatile String protocolVersion;

    public McpToolClient(SdkSyncClientDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String protocolVersion() {
        return protocolVersion;
    }

    @Override
    public synchronized McpListToolsResult listTools() throws Exception {
        initialize();
        logger.info("Listing MCP tools.");
        McpSchema.ListToolsResult result = delegate.listTools();
        JsonNode raw = JsonUtils.valueToTree(result);
        return new McpListToolsResult(raw.path("tools"), raw);
    }

    @Override
    public synchronized McpToolCallResult callTool(String toolName,
                                                   Map<String, Object> arguments,
                                                   ProgressListener progressListener) throws Exception {
        initialize();
        logger.info("Calling MCP tool. toolName={}", toolName);
        McpSchema.CallToolResult result = delegate.callTool(new McpSchema.CallToolRequest(toolName, arguments));
        JsonNode raw = JsonUtils.valueToTree(result);
        if (progressListener != null) {
            progressListener.onProgress("Handled by MCP Java SDK client", null, null);
        }
        return new McpToolCallResult(
                raw.path("content"),
                raw.path("structuredContent"),
                raw.has("isError") ? raw.path("isError").asBoolean() : null,
                raw
        );
    }

    @Override
    public void closeSession() throws Exception {
        logger.info("Closing MCP client.");
        delegate.closeGracefully();
    }

    private void initialize() {
        logger.info("Initializing MCP client for current sampler invocation.");
        McpSchema.InitializeResult result = delegate.initialize();
        protocolVersion = result == null ? null : result.protocolVersion();
        logger.info("MCP client initialized. protocolVersion={}", protocolVersion);
    }
}
