package org.apache.jmeter.mcp.ms;

import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.mcp.util.JsonUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;

import java.util.ArrayList;
import java.util.List;

public final class McpElementUtil {
    private static final String STEP_DELIMITER = "^@~@^";
    private static final String SEPARATOR = "<->";

    private McpElementUtil() {
    }

    public static void setBaseParams(McpSamplerBase sampler, MsTestElement element, MsParameter config) {
        String id = (element.getId() != null && !element.getId().isBlank())
                ? element.getId() : element.getResourceId();
        sampler.setProperty("MS-ID", id);
        sampler.setProperty("MS-RESOURCE-ID", getResourceId(id, element.getParent(), element.getIndex()));
        List<String> scenarios = new ArrayList<>();
        collectScenarioSet(element.getParent(), scenarios);
        sampler.setProperty("MS-SCENARIO", JsonUtils.toJson(scenarios));
        sampler.setProperty("MS-PATH", getParentName(element.getParent()));
        sampler.setProperty("RESULT_CLASS", "org.apache.jmeter.samplers.SampleResult");
    }

    private static void collectScenarioSet(MsTestElement element, List<String> values) {
        if (element == null) {
            return;
        }
        if ("scenario".equals(element.getType())) {
            values.add(element.getResourceId() + "_" + element.getName());
        }
        collectScenarioSet(element.getParent(), values);
    }

    private static String getParentName(MsTestElement parent) {
        if (parent == null) {
            return "";
        }
        return getFullPath(parent, "") + SEPARATOR + parent.getName();
    }

    private static String getFullPath(MsTestElement element, String path) {
        if (element == null || element.getParent() == null) {
            return path;
        }
        String name = (element.getName() == null || element.getName().isBlank()) ? element.getType() : element.getName();
        String next = name + STEP_DELIMITER + path;
        return getFullPath(element.getParent(), next);
    }

    private static String getResourceId(String resourceId, MsTestElement parent, String indexPath) {
        return resourceId + "_" + getFullIndexPath(parent, indexPath);
    }

    private static String getFullIndexPath(MsTestElement element, String path) {
        if (element == null || element.getParent() == null) {
            return path;
        }
        String next = element.getIndex() + "_" + path;
        return getFullIndexPath(element.getParent(), next);
    }
}
