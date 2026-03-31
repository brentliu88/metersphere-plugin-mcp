# MeterSphere MCP Plugin 结构

```text
src/main/java/com/example/metersphere/mcp/
├── UiScriptApiImpl.java                       # MeterSphere UI 注册入口
├── auth/                                      # 鉴权头策略，提供 SDK transport 需要的 HTTP headers
├── client/
│   ├── McpClientRegistry.java                 # SDK client 进程内缓存，按 clientKey 复用
│   ├── McpSessionContext.java                 # MeterSphere/JMeter 侧会话上下文
│   ├── McpToolClient.java                     # SDK 客户端高层封装（initialize/list/call）
│   ├── ProgressListener.java
│   └── SdkMcpClientFactory.java               # Java SDK transport/client 构造
├── model/                                     # MeterSphere 结果模型
├── ms/                                        # MsTestElement 封装层
│   ├── McpElementUtil.java
│   ├── MsMcpSamplerBase.java
│   ├── MsMcpInitializeSampler.java
│   ├── MsMcpToolsListSampler.java
│   └── MsMcpToolsCallSampler.java
├── runtime/
│   ├── McpSamplerBase.java
│   └── McpSamplerSupport.java                 # config/context/registry 装配
├── sampler/
│   ├── McpInitializeSampler.java
│   ├── McpToolsListSampler.java
│   └── McpToolsCallSampler.java
└── util/

src/main/resources/
├── plugin.properties
└── json/
    ├── mcp-ui_form.json
    ├── mcp-ui_initialize.json
    ├── mcp-ui_tools_list.json
    └── mcp-ui_tools_call.json
```

## 架构变化

- 已移除“插件自己实现 Streamable HTTP/JSON-RPC”的主路径
- 改为“MeterSphere sampler -> SDK client registry -> MCP Java SDK client”
- `sessionId` 输入改为 `clientKey`
- `MCP_SESSION_ID` 变量改为 `MCP_CLIENT_KEY`
