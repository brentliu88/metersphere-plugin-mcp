package org.apache.jmeter.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;

public interface SdkSyncClientDelegate {
    boolean isInitialized();

    McpSchema.InitializeResult getCurrentInitializationResult();

    McpSchema.InitializeResult initialize();

    McpSchema.ListToolsResult listTools();

    McpSchema.CallToolResult callTool(McpSchema.CallToolRequest request);

    boolean closeGracefully();
}
