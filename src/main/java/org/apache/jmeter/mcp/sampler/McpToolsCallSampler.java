package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpToolCallResult;
import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.mcp.runtime.McpSamplerSupport;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class McpToolsCallSampler extends McpSamplerBase {
    private static final Logger logger = LoggerFactory.getLogger(McpToolsCallSampler.class);

    @Override
    public SampleResult sample(Entry entry) {
        String toolName = getResolvedPropertyOrDefault("toolName", "");
        SampleResult result = startSample("MCP tools/call - " + toolName);
        StringBuilder progressLog = new StringBuilder();
        try {
            logger.info("Starting MCP tools/call sampler. toolName={}", toolName);
            McpClientConfig config = McpSamplerSupport.buildConfig(this);
            try (McpOperations client = McpSamplerSupport.buildClient(config)) {
                Map<String, Object> arguments = McpSamplerSupport.parseArguments(getResolvedProperty("toolArgumentsJson"));
                logger.info("MCP tools/call request. endpoint={}, toolName={}, arguments={}",
                        config.endpoint(), toolName, JsonUtils.toJson(arguments));
                McpToolCallResult toolResult = client.callTool(toolName, arguments, (message, progress, total) -> {
                    progressLog.append("message=").append(message)
                            .append(", progress=").append(progress)
                            .append(", total=").append(total)
                            .append('\n');
                });

                boolean hasToolError = toolResult.isError() != null && toolResult.isError();
                Map<String, Object> responsePayload = new LinkedHashMap<>();
                responsePayload.put("toolName", toolName);
                responsePayload.put("protocolVersion", safe(client.protocolVersion()));
                responsePayload.put("progressLog", progressLog.toString());
                responsePayload.put("result", toolResult.rawResult());
                String response = JsonUtils.toJson(responsePayload);
                saveResultVariable(getResolvedProperty("saveResultVariable"), response);
                logger.info("MCP tools/call sampler finished. toolName={}, protocolVersion={}, isError={}",
                        toolName, client.protocolVersion(), hasToolError);
                logger.info("MCP tools/call response: {}", response);
                endSuccess(result, hasToolError ? "MCP tools/call returned isError=true" : "MCP tools/call succeeded", response);
                if (hasToolError) {
                    result.setSuccessful(false);
                }
            }
        } catch (Exception ex) {
            logger.error("MCP tools/call sampler failed. toolName={}", toolName, ex);
            endFailure(result, ex);
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
