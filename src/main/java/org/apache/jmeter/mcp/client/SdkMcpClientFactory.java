package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.model.McpClientConfig;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Map;

final class SdkMcpClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(SdkMcpClientFactory.class);

    private SdkMcpClientFactory() {
    }

    static McpOperations create(McpClientConfig config, McpSessionContext sessionContext) {
        logger.info("Building MCP SDK transport. endpoint={}, connectTimeoutMs={}, requestTimeoutMs={}",
                config.endpoint(), config.connectTimeout().toMillis(), config.requestTimeout().toMillis());
        HttpClientStreamableHttpTransport.Builder transportBuilder = HttpClientStreamableHttpTransport
                .builder(config.endpoint().toString())
                .clientBuilder(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER))
                .connectTimeout(config.connectTimeout());

        Map<String, String> headers = config.authStrategy().headers();
        if (!headers.isEmpty()) {
            logger.info("Applying {} custom auth headers to MCP transport. endpoint={}", headers.size(), config.endpoint());
            transportBuilder.httpRequestCustomizer(requestCustomizer(headers));
        }

        McpSyncClient delegate = McpClient.sync(transportBuilder.build())
                .requestTimeout(config.requestTimeout())
                .initializationTimeout(config.requestTimeout())
                .clientInfo(new McpSchema.Implementation("metersphere-mcp-plugin", "0.1.0"))
                .capabilities(McpSchema.ClientCapabilities.builder().build())
                .build();
        logger.info("MCP SDK client created. endpoint={}", config.endpoint());
        return new McpToolClient(new SdkSyncClientAdapter(delegate), sessionContext);
    }

    private static McpSyncHttpClientRequestCustomizer requestCustomizer(Map<String, String> headers) {
        return (requestBuilder, method, uri, body, context) -> headers.forEach(requestBuilder::header);
    }
}
