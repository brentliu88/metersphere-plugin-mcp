package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.mcp.runtime.McpSamplerSupport;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class McpToolsListSampler extends McpSamplerBase {
    private static final Logger logger = LoggerFactory.getLogger(McpToolsListSampler.class);

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = startSample("MCP tools/list");
        try {
            logger.info("Starting MCP tools/list sampler.");
            McpClientConfig config = McpSamplerSupport.buildConfig(this);
            logger.info("MCP tools/list request. endpoint={}", config.endpoint());
            try (McpOperations client = McpSamplerSupport.buildClient(config)) {
                McpListToolsResult toolsResult = client.listTools();

                Map<String, Object> responsePayload = new LinkedHashMap<>();
                responsePayload.put("protocolVersion", safe(client.protocolVersion()));
                responsePayload.put("tools", toolsResult.tools());
                String response = JsonUtils.toJson(responsePayload);
                saveResultVariable(getResolvedProperty("saveResultVariable"), response);
                logger.info("MCP tools/list sampler succeeded. protocolVersion={}", client.protocolVersion());
                logger.info("MCP tools/list response: {}", response);
                endSuccess(result, "MCP tools/list succeeded", response);
            }
        } catch (Exception ex) {
            logger.error("MCP tools/list sampler failed.", ex);
            endFailure(result, ex);
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
