# MeterSphere MCP Plugin 结构

```text
src/main/java/org/apache/jmeter/mcp/
├── UiScriptApiImpl.java                       # MeterSphere UI 注册入口，只注册两个节点
├── auth/                                      # 鉴权头策略
├── client/
│   ├── McpOperations.java                     # sampler 使用的客户端抽象
│   ├── McpToolClient.java                     # SDK 客户端高层封装（list/call）
│   ├── ProgressListener.java
│   ├── SdkMcpClientFactory.java               # Java SDK transport/client 构造
│   ├── SdkSyncClientAdapter.java              # SDK 同步客户端适配
│   └── SdkSyncClientDelegate.java
├── model/                                     # MeterSphere 结果模型
├── ms/                                        # MsTestElement 封装层
│   ├── McpElementUtil.java
│   ├── MsMcpSamplerBase.java
│   ├── MsMcpToolsListSampler.java
│   └── MsMcpToolsCallSampler.java
├── runtime/
│   ├── McpSamplerBase.java
│   └── McpSamplerSupport.java                 # config 与客户端创建
├── sampler/
│   ├── McpToolsListSampler.java
│   └── McpToolsCallSampler.java
└── util/

src/main/resources/
├── plugin.properties
└── json/
    ├── mcp-ui_form.json
    ├── mcp-ui_tools_list.json
    └── mcp-ui_tools_call.json
```

## 当前架构

- 已删除独立 `initialize` sampler
- 已删除插件侧 session/clientKey 复用逻辑
- 当前调用链路为：
  `MeterSphere sampler -> McpSamplerSupport -> SdkMcpClientFactory -> McpToolClient -> MCP Java SDK`
