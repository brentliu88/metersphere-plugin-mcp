package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.client.McpSessionContext;
import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpListToolsResult;
import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.mcp.runtime.McpSamplerSupport;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class McpToolsListSampler extends McpSamplerBase {
    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = startSample("MCP tools/list");
        try {
            McpClientConfig config = McpSamplerSupport.buildConfig(this);
            McpSessionContext sessionContext = McpSamplerSupport.buildSessionContext(this);
            McpOperations client = McpSamplerSupport.buildClient(config, sessionContext);

            boolean autoInitialize = getBooleanProperty("autoInitialize", true);
            if (autoInitialize && (sessionContext.negotiatedProtocolVersion() == null
                    || sessionContext.negotiatedProtocolVersion().isBlank())) {
                client.initialize();
            }

            McpListToolsResult toolsResult = client.listTools();
            McpSamplerSupport.persistSession(this, sessionContext);

            Map<String, Object> responsePayload = new LinkedHashMap<>();
            responsePayload.put("clientKey", safe(sessionContext.clientKey()));
            responsePayload.put("protocolVersion", safe(sessionContext.negotiatedProtocolVersion()));
            responsePayload.put("tools", toolsResult.tools());
            String response = JsonUtils.toJson(responsePayload);
            saveResultVariable(getResolvedProperty("saveResultVariable"), response);
            endSuccess(result, "MCP tools/list succeeded", response);
        } catch (Exception ex) {
            endFailure(result, ex);
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
