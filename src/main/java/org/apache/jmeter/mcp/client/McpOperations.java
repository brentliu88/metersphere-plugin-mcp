package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.model.McpInitializeResult;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;

import java.util.Map;

public interface McpOperations {
    void adopt(McpSessionContext sessionContext);

    McpInitializeResult initialize() throws Exception;

    McpListToolsResult listTools() throws Exception;

    McpToolCallResult callTool(String toolName, Map<String, Object> arguments, ProgressListener progressListener) throws Exception;

    void closeSession() throws Exception;
}
