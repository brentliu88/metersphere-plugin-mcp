# MeterSphere MCP 插件详细设计

## 1. 目标

提供基于 MCP Java SDK 的两个 MeterSphere 节点：

1. `MCP Tools List`
2. `MCP Tools Call`

插件不暴露独立初始化节点，也不在插件层维护会话。

## 2. 核心架构

```mermaid
flowchart LR
    A[MeterSphere UI] --> B[MsTestElement]
    B --> C[JMeter Sampler]
    C --> D[McpSamplerSupport]
    D --> E[SdkMcpClientFactory]
    E --> F[McpToolClient]
    F --> G[MCP Java SDK]
    G --> H[Remote MCP Server]
```

## 3. 设计原则

1. 节点数量最小化，只保留用户真正操作的 `list` 与 `call`。
2. 初始化细节下沉到客户端内部。
3. 插件保持无状态，避免 session 变量、连接缓存和跨节点运行期耦合。

## 4. 调用模型

### 4.1 Tools List

- 读取 sampler 配置
- 创建 SDK client
- 执行 `initialize`
- 执行 `tools/list`
- 返回结果
- 关闭 SDK client

### 4.2 Tools Call

- 读取 sampler 配置
- 创建 SDK client
- 执行 `initialize`
- 执行 `tools/call`
- 返回结果
- 关闭 SDK client

## 5. 关键类职责

- `SdkMcpClientFactory`
  - 创建 SDK transport 与 SDK client
  - 注入鉴权 header
- `McpToolClient`
  - 封装内部初始化流程
  - 暴露 `listTools()` 与 `callTool()`
  - 保存本次调用协商得到的 `protocolVersion`
- `McpSamplerSupport`
  - 从 MeterSphere/JMeter 参数构造 `McpClientConfig`
  - 创建新的无状态客户端

## 6. 表单字段

### 保留

- `baseUrl`
- `connectTimeoutMs`
- `requestTimeoutMs`
- `authorizationType`
- `bearerToken`
- `apiKeyHeaderName`
- `apiKeyValue`
- `customHeadersJson`
- `saveResultVariable`

### 移除

- 独立 `initialize` 节点
- `autoInitialize`
- `clientKey`
- 插件侧 session 相关变量

## 7. 已知限制

1. 每个 sampler 都会重新初始化一次 MCP client。
2. 当前设计不会跨 sampler 共享连接或协议状态。
3. 如果未来需要长连接或共享会话，需要单独引入新的运行时设计，而不是恢复旧的插件内 session 变量方案。
