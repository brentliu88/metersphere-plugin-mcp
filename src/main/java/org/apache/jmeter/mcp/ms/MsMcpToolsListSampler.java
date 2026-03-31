package org.apache.jmeter.mcp.ms;

import org.apache.jmeter.mcp.sampler.McpToolsListSampler;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.plugin.core.utils.LogUtil;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

public class MsMcpToolsListSampler extends MsMcpSamplerBase {
    private String clazzName = "org.apache.jmeter.mcp.ms.MsMcpToolsListSampler";

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter config) {
        LogUtil.info("transform MsMcpToolsListSampler");
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

    public McpToolsListSampler initSampler(MsParameter config) {
        McpToolsListSampler sampler = new McpToolsListSampler();
        applyCommonProperties(
                sampler,
                config,
                "org.apache.jmeter.mcp.sampler.McpToolsListSamplerUI",
                "org.apache.jmeter.mcp.sampler.McpToolsListSampler"
        );
        return sampler;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }
}
