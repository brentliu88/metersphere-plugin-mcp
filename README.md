# MeterSphere MCP Plugin

This plugin uses the MCP Java SDK as its MCP client layer.

## Runtime model

- Only two sampler nodes are exposed:
  - `MCP Tools List`
  - `MCP Tools Call`
- There is no plugin-level session concept
- Each sampler invocation creates a fresh SDK client, initializes it internally, executes the operation, and closes it
- The plugin does not persist `clientKey`, `sessionId`, or MCP protocol state between samplers

## Supported node behavior

- `MCP Tools List`
  - creates a new SDK client
  - initializes it implicitly
  - calls `tools/list`
  - returns `protocolVersion` and `tools`
- `MCP Tools Call`
  - creates a new SDK client
  - initializes it implicitly
  - calls `tools/call`
  - returns `toolName`, `protocolVersion`, `progressLog`, and `result`

## Build

```bash
mvn -q -DskipTests compile
```

## Test And Coverage

```bash
mvn -q test
```

Current JaCoCo line coverage is about `85.87%`.

## Key entry points

- `org.apache.jmeter.mcp.UiScriptApiImpl`
- `org.apache.jmeter.mcp.client.SdkMcpClientFactory`
- `org.apache.jmeter.mcp.client.McpToolClient`
- `org.apache.jmeter.mcp.runtime.McpSamplerSupport`
- `org.apache.jmeter.mcp.sampler.McpToolsListSampler`
- `org.apache.jmeter.mcp.sampler.McpToolsCallSampler`
