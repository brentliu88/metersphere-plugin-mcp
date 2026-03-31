# MeterSphere MCP Plugin

This plugin now uses the MCP Java SDK as its MCP client layer instead of the in-repo `java.net.http` transport.

## Runtime model

- MeterSphere UI and sampler nodes stay the same: `MCP Initialize`, `MCP Tools List`, `MCP Tools Call`
- MCP protocol execution is delegated to the Java SDK client
- Streamable HTTP session state is kept inside an in-process SDK client registry keyed by `clientKey`
- JMeter variables persisted by the plugin:
  - `MCP_CLIENT_KEY`
  - `MCP_PROTOCOL_VERSION`

## Why `clientKey` replaced `sessionId`

The old implementation persisted raw `MCP-Session-Id` values and rebuilt HTTP requests manually.
After moving to the Java SDK, session lifecycle is owned by the SDK transport/client pair, so the plugin now reuses a live SDK client instance inside the same MeterSphere/JMeter process.

Use the optional `clientKey` field when multiple nodes must share the same SDK-backed MCP session.
If omitted, the plugin creates a new key automatically during `MCP Initialize`.

## Supported node behavior

- `MCP Initialize`
  - creates or reuses an SDK client from the registry
  - calls SDK `initialize()`
  - stores `clientKey` and negotiated protocol version
- `MCP Tools List`
  - reuses the SDK client identified by `clientKey`
  - optionally auto-initializes
- `MCP Tools Call`
  - reuses the SDK client identified by `clientKey`
  - optionally auto-initializes

## Build

```bash
mvn -q -DskipTests compile
```

## Dependency note

The POM is pinned to `io.modelcontextprotocol.sdk:mcp:${mcp.sdk.version}` with `mcp.sdk.version=1.1.1`.
The current workspace compiled successfully against that SDK version.

## Key entry points

- `org.apache.jmeter.mcp.UiScriptApiImpl`
- `org.apache.jmeter.mcp.client.McpClientRegistry`
- `org.apache.jmeter.mcp.client.SdkMcpClientFactory`
- `org.apache.jmeter.mcp.client.McpToolClient`
- `org.apache.jmeter.mcp.runtime.McpSamplerSupport`
