# MCP Java SDK 集成说明

当前代码已经把运行时主路径切换到 MCP Java SDK 客户端。

## 当前约束

1. MeterSphere 多节点会话复用不再依赖裸 `MCP-Session-Id` 头，而是依赖进程内 `clientKey -> SDK client` 映射。
2. `clientKey` 必须在同一次执行进程内复用；跨进程、跨重启不能恢复同一个 SDK client。
3. `MCP_PROTOCOL_VERSION` 仍然会在初始化后持久化，供后续断言和结果展示使用。
4. 鉴权仍由插件表单控制，但最终通过 SDK transport 的 HTTP request customizer 注入 Header。

## 后续建议

1. 确认你实际可用的 Java SDK Maven 坐标和版本号，再最终锁定 `pom.xml`。
2. 如果你需要显式释放 SDK client，可增加一个 `MCP Close` 节点，对 `McpClientRegistry` 中的对象执行 `closeGracefully()`.
3. 如果后续要支持跨进程恢复，需要等 SDK 暴露更稳定的 session persistence 能力，或插件自己补充外部状态存储。
