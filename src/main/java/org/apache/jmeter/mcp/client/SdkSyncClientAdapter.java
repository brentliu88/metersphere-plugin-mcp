package org.apache.jmeter.mcp.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

public final class SdkSyncClientAdapter implements SdkSyncClientDelegate {
    private final McpSyncClient delegate;

    public SdkSyncClientAdapter(McpSyncClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isInitialized() {
        return delegate.isInitialized();
    }

    @Override
    public McpSchema.InitializeResult getCurrentInitializationResult() {
        return delegate.getCurrentInitializationResult();
    }

    @Override
    public McpSchema.InitializeResult initialize() {
        return delegate.initialize();
    }

    @Override
    public McpSchema.ListToolsResult listTools() {
        return delegate.listTools();
    }

    @Override
    public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest request) {
        return delegate.callTool(request);
    }

    @Override
    public boolean closeGracefully() {
        return delegate.closeGracefully();
    }
}
