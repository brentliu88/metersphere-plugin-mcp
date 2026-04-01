package org.apache.jmeter.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpToolClientTest {
    @Test
    void listToolsInitializesBeforeListing() throws Exception {
        FakeDelegate delegate = new FakeDelegate();
        delegate.initializeResult = new McpSchema.InitializeResult("2025-03-26", null, null, null);
        delegate.listToolsResult = new McpSchema.ListToolsResult(List.of(), null);
        McpToolClient client = new McpToolClient(delegate);

        McpListToolsResult result = client.listTools();

        assertEquals(1, delegate.initializeCalls);
        assertEquals(1, delegate.listCalls);
        assertEquals("2025-03-26", client.protocolVersion());
        assertTrue(result.tools().isArray());
    }

    @Test
    void callToolInitializesAndReturnsProgressMessage() throws Exception {
        FakeDelegate delegate = new FakeDelegate();
        delegate.initializeResult = new McpSchema.InitializeResult("2025-03-26", null, null, null);
        delegate.callToolResult = new McpSchema.CallToolResult(List.of(), false, Map.of("ok", true), Map.of());
        McpToolClient client = new McpToolClient(delegate);
        AtomicReference<String> progressMessage = new AtomicReference<>();

        McpToolCallResult result = client.callTool("echo", Map.of("a", 1), (message, progress, total) -> progressMessage.set(message));

        assertEquals(1, delegate.initializeCalls);
        assertEquals("echo", delegate.lastToolName);
        assertEquals(Map.of("a", 1), delegate.lastArguments);
        assertFalse(result.isError());
        assertEquals(true, result.structuredContent().path("ok").asBoolean());
        assertEquals("Handled by MCP Java SDK client", progressMessage.get());
    }

    @Test
    void nullInitializeResultLeavesProtocolVersionNull() throws Exception {
        FakeDelegate delegate = new FakeDelegate();
        delegate.initializeResult = null;
        delegate.listToolsResult = new McpSchema.ListToolsResult(List.of(), null);
        McpToolClient client = new McpToolClient(delegate);

        client.listTools();

        assertNull(client.protocolVersion());
    }

    @Test
    void closeSessionDelegates() throws Exception {
        FakeDelegate delegate = new FakeDelegate();
        McpToolClient client = new McpToolClient(delegate);

        client.closeSession();

        assertTrue(delegate.closed);
    }

    private static final class FakeDelegate implements SdkSyncClientDelegate {
        private McpSchema.InitializeResult initializeResult;
        private McpSchema.ListToolsResult listToolsResult;
        private McpSchema.CallToolResult callToolResult;
        private int initializeCalls;
        private int listCalls;
        private boolean closed;
        private String lastToolName;
        private Map<String, Object> lastArguments;

        @Override
        public McpSchema.InitializeResult initialize() {
            initializeCalls++;
            return initializeResult;
        }

        @Override
        public McpSchema.ListToolsResult listTools() {
            listCalls++;
            return listToolsResult;
        }

        @Override
        public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest request) {
            lastToolName = request.name();
            lastArguments = request.arguments();
            return callToolResult;
        }

        @Override
        public boolean closeGracefully() {
            closed = true;
            return true;
        }
    }
}
