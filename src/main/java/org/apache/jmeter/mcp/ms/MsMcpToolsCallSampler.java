package org.apache.jmeter.mcp.ms;

import org.apache.jmeter.mcp.sampler.McpToolsCallSampler;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.plugin.core.utils.LogUtil;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

public class MsMcpToolsCallSampler extends MsMcpSamplerBase {
    private String clazzName = "org.apache.jmeter.mcp.ms.MsMcpToolsCallSampler";
    private String toolName;
    private String toolArgumentsJson;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter config) {
        LogUtil.info("transform MsMcpToolsCallSampler");
        if (!this.isEnable()) {
            return;
        }
        HashTree pluginTree = tree.add(initSampler(config));
        if (hashTree != null && !hashTree.isEmpty()) {
            for (MsTestElement el : hashTree) {
                el.toHashTree(pluginTree, el.getHashTree(), config);
            }
        }
    }

    public McpToolsCallSampler initSampler(MsParameter config) {
        McpToolsCallSampler sampler = new McpToolsCallSampler();
        applyCommonProperties(
                sampler,
                config,
                "org.apache.jmeter.mcp.sampler.McpToolsCallSamplerUI",
                "org.apache.jmeter.mcp.sampler.McpToolsCallSampler"
        );
        sampler.setProperty("toolName", this.toolName);
        sampler.setProperty("toolArgumentsJson", this.toolArgumentsJson);
        return sampler;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolArgumentsJson() {
        return toolArgumentsJson;
    }

    public void setToolArgumentsJson(String toolArgumentsJson) {
        this.toolArgumentsJson = toolArgumentsJson;
    }
}
