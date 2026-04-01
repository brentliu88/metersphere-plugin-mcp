package org.apache.jmeter.mcp.client;

import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.model.McpToolCallResult;

import java.util.Map;

public interface McpOperations extends AutoCloseable {
    String protocolVersion();

    McpListToolsResult listTools() throws Exception;

    McpToolCallResult callTool(String toolName, Map<String, Object> arguments, ProgressListener progressListener) throws Exception;

    void closeSession() throws Exception;

    @Override
    default void close() throws Exception {
        closeSession();
    }
}
