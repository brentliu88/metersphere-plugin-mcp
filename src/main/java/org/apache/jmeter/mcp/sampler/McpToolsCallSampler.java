package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.client.McpSessionContext;
import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.mcp.runtime.McpSamplerSupport;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class McpToolsCallSampler extends McpSamplerBase {
    @Override
    public SampleResult sample(Entry entry) {
        String toolName = getResolvedPropertyOrDefault("toolName", "");
        SampleResult result = startSample("MCP tools/call - " + toolName);
        StringBuilder progressLog = new StringBuilder();
        try {
            McpClientConfig config = McpSamplerSupport.buildConfig(this);
            McpSessionContext sessionContext = McpSamplerSupport.buildSessionContext(this);
            McpOperations client = McpSamplerSupport.buildClient(config, sessionContext);

            boolean autoInitialize = getBooleanProperty("autoInitialize", true);
            if (autoInitialize && (sessionContext.negotiatedProtocolVersion() == null
                    || sessionContext.negotiatedProtocolVersion().isBlank())) {
                client.initialize();
            }

            Map<String, Object> arguments = McpSamplerSupport.parseArguments(getResolvedProperty("toolArgumentsJson"));
            McpToolCallResult toolResult = client.callTool(toolName, arguments, (message, progress, total) -> {
                progressLog.append("message=").append(message)
                        .append(", progress=").append(progress)
                        .append(", total=").append(total)
                        .append('\n');
            });
            McpSamplerSupport.persistSession(this, sessionContext);

            boolean hasToolError = toolResult.isError() != null && toolResult.isError();
            Map<String, Object> responsePayload = new LinkedHashMap<>();
            responsePayload.put("toolName", toolName);
            responsePayload.put("clientKey", safe(sessionContext.clientKey()));
            responsePayload.put("protocolVersion", safe(sessionContext.negotiatedProtocolVersion()));
            responsePayload.put("progressLog", progressLog.toString());
            responsePayload.put("result", toolResult.rawResult());
            String response = JsonUtils.toJson(responsePayload);
            saveResultVariable(getResolvedProperty("saveResultVariable"), response);
            endSuccess(result, hasToolError ? "MCP tools/call returned isError=true" : "MCP tools/call succeeded", response);
            if (hasToolError) {
                result.setSuccessful(false);
            }
        } catch (Exception ex) {
            endFailure(result, ex);
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
