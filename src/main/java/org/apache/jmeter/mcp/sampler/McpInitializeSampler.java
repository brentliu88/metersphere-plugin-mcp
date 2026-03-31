package org.apache.jmeter.mcp.sampler;

import org.apache.jmeter.mcp.client.McpSessionContext;
import org.apache.jmeter.mcp.client.McpOperations;
import org.apache.jmeter.mcp.model.McpClientConfig;
import org.apache.jmeter.mcp.model.McpInitializeResult;
import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.mcp.runtime.McpSamplerSupport;
import org.apache.jmeter.mcp.util.JsonUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class McpInitializeSampler extends McpSamplerBase {
    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = startSample("MCP Initialize");
        try {
            McpClientConfig config = McpSamplerSupport.buildConfig(this);
            McpSessionContext sessionContext = McpSamplerSupport.buildSessionContext(this);
            McpOperations client = McpSamplerSupport.buildClient(config, sessionContext);

            McpInitializeResult initializeResult = client.initialize();
            McpSamplerSupport.persistSession(this, sessionContext);

            Map<String, Object> responsePayload = new LinkedHashMap<>();
            responsePayload.put("clientKey", safe(initializeResult.clientKey()));
            responsePayload.put("protocolVersion", safe(initializeResult.protocolVersion()));
            responsePayload.put("capabilities", initializeResult.capabilities());
            responsePayload.put("serverInfo", initializeResult.serverInfo());
            String response = JsonUtils.toJson(responsePayload);
            saveResultVariable(getResolvedProperty("saveResultVariable"), response);
            endSuccess(result, "MCP initialize succeeded", response);
        } catch (Exception ex) {
            endFailure(result, ex);
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
