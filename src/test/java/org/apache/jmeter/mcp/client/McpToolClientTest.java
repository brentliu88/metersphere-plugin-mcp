package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.model.McpInitializeResult;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpToolClientTest {
    @Mock
    private SdkSyncClientDelegate delegate;

    private McpSessionContext context;

    @BeforeEach
    void setUp() {
        context = new McpSessionContext();
        context.clientKey("client-1");
    }

    @Test
    void initializeCachesInitializationResult() throws Exception {
        when(delegate.isInitialized()).thenReturn(false);
        when(delegate.initialize()).thenReturn(new McpSchema.InitializeResult(
                "2025-03-26",
                null,
                new McpSchema.Implementation("server", "1.0.0"),
                "instructions"
        ));

        McpToolClient client = new McpToolClient(delegate, context);
        McpInitializeResult first = client.initialize();
        McpInitializeResult second = client.initialize();

        assertEquals("2025-03-26", first.protocolVersion());
        assertEquals("client-1", first.clientKey());
        assertEquals("server", first.serverInfo().path("name").asText());
        assertEquals("2025-03-26", second.protocolVersion());
        verify(delegate, times(1)).initialize();
    }

    @Test
    void adoptCopiesCurrentInitializationStateFromDelegate() {
        when(delegate.isInitialized()).thenReturn(true);
        when(delegate.getCurrentInitializationResult()).thenReturn(new McpSchema.InitializeResult(
                "2025-03-26",
                null,
                new McpSchema.Implementation("server", "1.0.0"),
                "instructions"
        ));
        McpToolClient client = new McpToolClient(delegate, context);
        McpSessionContext adopted = new McpSessionContext();
        adopted.clientKey("client-2");

        client.adopt(adopted);

        assertEquals("2025-03-26", adopted.negotiatedProtocolVersion());
        assertEquals("server", adopted.serverInfo().path("name").asText());
    }

    @Test
    void listToolsAutoInitializesWhenNeeded() throws Exception {
        when(delegate.isInitialized()).thenReturn(false);
        when(delegate.initialize()).thenReturn(new McpSchema.InitializeResult(
                "2025-03-26",
                null,
                new McpSchema.Implementation("server", "1.0.0"),
                "instructions"
        ));
        when(delegate.listTools()).thenReturn(new McpSchema.ListToolsResult(List.of(), null));

        McpToolClient client = new McpToolClient(delegate, context);
        McpListToolsResult result = client.listTools();

        assertTrue(result.tools().isArray());
        verify(delegate).initialize();
        verify(delegate).listTools();
    }

    @Test
    void callToolReturnsStructuredResultAndProgressMessage() throws Exception {
        when(delegate.isInitialized()).thenReturn(true);
        when(delegate.getCurrentInitializationResult()).thenReturn(new McpSchema.InitializeResult(
                "2025-03-26",
                null,
                new McpSchema.Implementation("server", "1.0.0"),
                "instructions"
        ));
        when(delegate.callTool(any())).thenReturn(new McpSchema.CallToolResult(List.of(), false, Map.of("ok", true), Map.of()));
        AtomicReference<String> progressMessage = new AtomicReference<>();

        McpToolClient client = new McpToolClient(delegate, context);
        McpToolCallResult result = client.callTool("echo", Map.of("a", 1), (message, progress, total) -> progressMessage.set(message));

        assertFalse(result.isError());
        assertEquals(true, result.structuredContent().path("ok").asBoolean());
        assertEquals("Handled by MCP Java SDK client", progressMessage.get());
    }

    @Test
    void closeSessionDelegatesGracefully() throws Exception {
        when(delegate.isInitialized()).thenReturn(false);
        when(delegate.closeGracefully()).thenReturn(true);
        McpToolClient client = new McpToolClient(delegate, context);

        client.closeSession();

        verify(delegate).closeGracefully();
    }

    @Test
    void nullInitializeResultDoesNotOverwriteContext() throws Exception {
        when(delegate.isInitialized()).thenReturn(false);
        when(delegate.initialize()).thenReturn(null);
        McpToolClient client = new McpToolClient(delegate, context);

        McpInitializeResult result = client.initialize();

        assertNull(result.protocolVersion());
        assertNull(result.serverInfo());
    }
}
