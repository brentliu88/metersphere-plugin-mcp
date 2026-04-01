# MCP Java SDK 集成说明

当前实现是无状态设计。

## 当前行为

1. 插件只保留 `tools/list` 和 `tools/call` 两个 sampler。
2. 每次 sampler 执行都会新建一个 SDK client。
3. `initialize` 已并入这两个 sampler 的内部执行流程。
4. 插件不维护 `sessionId`、`clientKey` 或跨 sampler 的会话复用。

## 后续建议

1. 如果后续确实需要跨节点共享连接，再单独设计新的连接管理节点或运行时缓存层。
2. 如果只需要当前简单模型，保持无状态实现更稳妥，避免 MeterSphere/JMeter 运行期状态耦合。
