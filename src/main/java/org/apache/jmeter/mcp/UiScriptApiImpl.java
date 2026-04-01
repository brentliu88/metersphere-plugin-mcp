package org.apache.jmeter.mcp;

import io.metersphere.plugin.core.api.UiScriptApi;
import io.metersphere.plugin.core.ui.PluginResource;
import io.metersphere.plugin.core.ui.UiScript;
import io.metersphere.plugin.core.utils.LogUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class UiScriptApiImpl extends UiScriptApi {
    public boolean xpack() {
        return true;
    }

    @Override
    public PluginResource init() {
        LogUtil.info("init MCP ui scripts");
        List<UiScript> uiScripts = new LinkedList<>();

        UiScript toolsList = new UiScript(
                "mcp_tools_list",
                "MCP Tools List",
                "org.apache.jmeter.mcp.ms.MsMcpToolsListSampler",
                getJson("/json/mcp-ui_tools_list.json")
        );
        toolsList.setJmeterClazz("AbstractSampler");
        toolsList.setFormOption(getJson("/json/mcp-ui_form.json"));
        uiScripts.add(toolsList);

        UiScript toolsCall = new UiScript(
                "mcp_tools_call",
                "MCP Tools Call",
                "org.apache.jmeter.mcp.ms.MsMcpToolsCallSampler",
                getJson("/json/mcp-ui_tools_call.json")
        );
        toolsCall.setJmeterClazz("AbstractSampler");
        toolsCall.setFormOption(getJson("/json/mcp-ui_form.json"));
        uiScripts.add(toolsCall);

        return new PluginResource("mcp-v0.1.0", uiScripts);
    }

    @Override
    public String customMethod(String req) {
        LogUtil.info("customMethod request={}", req);
        return null;
    }

    private String getJson(String path) {
        try (InputStream in = UiScriptApiImpl.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LogUtil.error("failed to load json {}", path, ex);
            return null;
        }
    }
}
