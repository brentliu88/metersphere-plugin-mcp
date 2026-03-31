package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.auth.NoAuthStrategy;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class McpClientRegistryTest {
    @Test
    void createsClientAndStoresGeneratedKey() {
        McpOperations client = mock(McpOperations.class);
        McpClientConfig config = config("https://example.com/mcp");
        McpSessionContext context = new McpSessionContext();

        try (MockedStatic<SdkMcpClientFactory> factory = mockStatic(SdkMcpClientFactory.class)) {
            factory.when(() -> SdkMcpClientFactory.create(config, context)).thenReturn(client);

            McpOperations resolved = McpClientRegistry.getOrCreate(config, context);

            assertSame(client, resolved);
            assertEquals(true, context.clientKey().startsWith("mcp-sdk-"));
            verify(client, times(1)).adopt(context);
        }
    }

    @Test
    void reusesExistingClientForSameKeyAndFingerprint() {
        McpOperations client = mock(McpOperations.class);
        McpClientConfig config = config("https://reuse.example.com/mcp");
        McpSessionContext first = new McpSessionContext();
        first.clientKey("reuse-key");
        McpSessionContext second = new McpSessionContext();
        second.clientKey("reuse-key");

        try (MockedStatic<SdkMcpClientFactory> factory = mockStatic(SdkMcpClientFactory.class)) {
            factory.when(() -> SdkMcpClientFactory.create(config, first)).thenReturn(client);

            McpOperations a = McpClientRegistry.getOrCreate(config, first);
            McpOperations b = McpClientRegistry.getOrCreate(config, second);

            assertSame(a, b);
            factory.verify(() -> SdkMcpClientFactory.create(config, first), times(1));
            verify(client, times(3)).adopt(org.mockito.ArgumentMatchers.any(McpSessionContext.class));
        }
    }

    @Test
    void rejectsReuseForDifferentFingerprint() {
        McpOperations client = mock(McpOperations.class);
        McpClientConfig firstConfig = config("https://conflict.example.com/mcp");
        McpClientConfig secondConfig = config("https://other.example.com/mcp");
        McpSessionContext first = new McpSessionContext();
        first.clientKey("conflict-key");
        McpSessionContext second = new McpSessionContext();
        second.clientKey("conflict-key");

        try (MockedStatic<SdkMcpClientFactory> factory = mockStatic(SdkMcpClientFactory.class)) {
            factory.when(() -> SdkMcpClientFactory.create(firstConfig, first)).thenReturn(client);
            McpClientRegistry.getOrCreate(firstConfig, first);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> McpClientRegistry.getOrCreate(secondConfig, second));

            assertEquals(true, ex.getMessage().contains("already bound"));
        }
    }

    private static McpClientConfig config(String endpoint) {
        return new McpClientConfig(
                URI.create(endpoint),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2),
                new NoAuthStrategy()
        );
    }
}
