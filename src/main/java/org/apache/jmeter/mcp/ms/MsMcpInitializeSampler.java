package org.apache.jmeter.mcp.ms;

import org.apache.jmeter.mcp.sampler.McpInitializeSampler;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.plugin.core.utils.LogUtil;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

public class MsMcpInitializeSampler extends MsMcpSamplerBase {
    private String clazzName = "org.apache.jmeter.mcp.ms.MsMcpInitializeSampler";

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter config) {
        LogUtil.info("transform MsMcpInitializeSampler");
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

    public McpInitializeSampler initSampler(MsParameter config) {
        McpInitializeSampler sampler = new McpInitializeSampler();
        applyCommonProperties(
                sampler,
                config,
                "org.apache.jmeter.mcp.sampler.McpInitializeSamplerUI",
                "org.apache.jmeter.mcp.sampler.McpInitializeSampler"
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
