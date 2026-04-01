package org.apache.jmeter.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;

public interface SdkSyncClientDelegate {
    McpSchema.InitializeResult initialize();

    McpSchema.ListToolsResult listTools();

    McpSchema.CallToolResult callTool(McpSchema.CallToolRequest request);

    boolean closeGracefully();
}
