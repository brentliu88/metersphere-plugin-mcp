# MeterSphere MCP 插件详细设计

## 1. 目标

将 MeterSphere 中的 MCP 调用链路切换为官方 Java SDK 客户端实现，不再由插件自己维护 Streamable HTTP、SSE 与 JSON-RPC 细节。

## 2. 核心架构

```mermaid
flowchart LR
    A[MeterSphere UI] --> B[MsTestElement]
    B --> C[JMeter Sampler]
    C --> D[McpSamplerSupport]
    D --> E[McpClientRegistry]
    E --> F[McpToolClient]
    F --> G[MCP Java SDK]
    G --> H[Remote MCP Server]
```

## 3. 设计原则

1. UI 参数尽量稳定，避免影响已有节点定义。
2. 协议细节下沉到 Java SDK。
3. 插件只保留 MeterSphere 装配、鉴权参数转换、结果序列化与进程内 client 复用能力。

## 4. 会话模型

### 4.1 旧模型

- 插件自己发送 HTTP 请求
- 插件自己读取 `MCP-Session-Id`
- 插件把 `sessionId` 存入 JMeter 变量

### 4.2 新模型

- Java SDK 自己管理 Streamable HTTP 会话
- 插件维护 `clientKey`
- `clientKey` 对应一个存活中的 SDK client
- 同一执行进程中的多个节点通过 `clientKey` 复用该 client

## 5. 关键类职责

- `McpClientRegistry`
  - 负责 `clientKey` 到 `McpToolClient` 的缓存
  - 防止同一个 `clientKey` 绑定不同连接配置
- `SdkMcpClientFactory`
  - 创建 SDK transport 与 SDK client
  - 把鉴权 header 注入 SDK HTTP request customizer
- `McpToolClient`
  - 提供 `initialize()` / `listTools()` / `callTool()`
  - 把 SDK 返回结果转换成插件内部 JSON 结果
- `McpSamplerSupport`
  - 从 MeterSphere/JMeter 参数构造 config 与 session context
  - 持久化 `MCP_CLIENT_KEY` 与 `MCP_PROTOCOL_VERSION`

## 6. 表单字段变化

### 保留

- `baseUrl`
- `connectTimeoutMs`
- `requestTimeoutMs`
- `authorizationType`
- `bearerToken`
- `apiKeyHeaderName`
- `apiKeyValue`
- `customHeadersJson`
- `autoInitialize`
- `saveResultVariable`

### 替换

- `sessionId` -> `clientKey`

### 移除

- 手工 `protocolVersion` 输入

说明:
协议版本改为以 SDK 初始化协商结果为准，不再由插件手动构造请求头强推。

## 7. 已知限制

1. `clientKey` 只在当前执行进程内有效。
2. 如果 SDK 或制品仓库中的实际 Maven 坐标与 `pom.xml` 中配置不一致，需要按你的仓库实际版本修正。
3. 这里的进度日志目前只保留了“由 SDK 路径处理”的占位信息，没有复刻旧 HTTP/SSE 客户端的逐条 progress 事件解析。
