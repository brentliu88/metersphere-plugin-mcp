package org.apache.jmeter.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;

public final class McpSessionContext {
    private volatile String clientKey;
    private volatile String negotiatedProtocolVersion;
    private volatile JsonNode capabilities;
    private volatile JsonNode serverInfo;

    public String clientKey() { return clientKey; }
    public void clientKey(String clientKey) { this.clientKey = clientKey; }

    public String negotiatedProtocolVersion() { return negotiatedProtocolVersion; }
    public void negotiatedProtocolVersion(String negotiatedProtocolVersion) { this.negotiatedProtocolVersion = negotiatedProtocolVersion; }

    public JsonNode capabilities() { return capabilities; }
    public void capabilities(JsonNode capabilities) { this.capabilities = capabilities; }

    public JsonNode serverInfo() { return serverInfo; }
    public void serverInfo(JsonNode serverInfo) { this.serverInfo = serverInfo; }
}
