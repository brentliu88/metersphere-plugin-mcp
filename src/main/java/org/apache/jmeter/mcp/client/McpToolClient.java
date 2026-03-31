package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.model.McpInitializeResult;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import org.apache.jmeter.mcp.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * High-level MCP client backed by the official MCP Java SDK.
 */
public final class McpToolClient implements McpOperations {
    private static final Logger logger = LoggerFactory.getLogger(McpToolClient.class);
    private final SdkSyncClientDelegate delegate;
    private volatile McpSessionContext sessionContext;
    private volatile boolean initialized;

    public McpToolClient(SdkSyncClientDelegate delegate, McpSessionContext sessionContext) {
        this.delegate = delegate;
        this.sessionContext = sessionContext;
        syncContextFromClient();
    }

    @Override
    public void adopt(McpSessionContext sessionContext) {
        this.sessionContext = sessionContext;
        syncContextFromClient();
    }

    @Override
    public synchronized McpInitializeResult initialize() throws Exception {
        if (!initialized) {
            logger.info("Initializing MCP client. clientKey={}", sessionContext.clientKey());
            McpSchema.InitializeResult result = delegate.initialize();
            applyInitializeResult(result);
            initialized = true;
            logger.info("MCP client initialized. clientKey={}, protocolVersion={}",
                    sessionContext.clientKey(), sessionContext.negotiatedProtocolVersion());
        }
        return new McpInitializeResult(
                sessionContext.negotiatedProtocolVersion(),
                sessionContext.capabilities(),
                sessionContext.serverInfo(),
                sessionContext.clientKey()
        );
    }

    @Override
    public McpListToolsResult listTools() throws Exception {
        ensureInitialized();
        logger.info("Listing MCP tools. clientKey={}", sessionContext.clientKey());
        McpSchema.ListToolsResult result = delegate.listTools();
        JsonNode raw = JsonUtils.valueToTree(result);
        return new McpListToolsResult(raw.path("tools"), raw);
    }

    @Override
    public McpToolCallResult callTool(String toolName,
                                      Map<String, Object> arguments,
                                      ProgressListener progressListener) throws Exception {
        ensureInitialized();
        logger.info("Calling MCP tool. clientKey={}, toolName={}", sessionContext.clientKey(), toolName);
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
        logger.info("Closing MCP client session. clientKey={}", sessionContext.clientKey());
        delegate.closeGracefully();
    }

    private void ensureInitialized() throws Exception {
        if (sessionContext.negotiatedProtocolVersion() == null || sessionContext.negotiatedProtocolVersion().isBlank()) {
            initialize();
        }
    }

    private void syncContextFromClient() {
        if (delegate.isInitialized()) {
            initialized = true;
            applyInitializeResult(delegate.getCurrentInitializationResult());
        }
    }

    private void applyInitializeResult(McpSchema.InitializeResult result) {
        if (result == null) {
            logger.warn("Received null MCP initialize result. clientKey={}", sessionContext.clientKey());
            return;
        }
        JsonNode raw = JsonUtils.valueToTree(result);
        sessionContext.negotiatedProtocolVersion(result.protocolVersion());
        sessionContext.capabilities(raw.path("capabilities"));
        sessionContext.serverInfo(raw.path("serverInfo"));
    }
}
