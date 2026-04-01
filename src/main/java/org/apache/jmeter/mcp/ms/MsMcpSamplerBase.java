package org.apache.jmeter.mcp.ms;

import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.jmeter.testelement.TestElement;

public abstract class MsMcpSamplerBase extends MsTestElement {
    private String comments;
    private String baseUrl;
    private String connectTimeoutMs;
    private String requestTimeoutMs;
    private String authorizationType;
    private String bearerToken;
    private String apiKeyHeaderName;
    private String apiKeyValue;
    private String customHeadersJson;
    private String saveResultVariable;

    protected void applyCommonProperties(McpSamplerBase sampler, MsParameter config, String guiClass, String testClass) {
        McpElementUtil.setBaseParams(sampler, this, config);
        sampler.setEnabled(this.isEnable());
        sampler.setName((this.getName() == null || this.getName().isBlank()) ? testClass : this.getName());
        sampler.setProperty(TestElement.GUI_CLASS, guiClass);
        sampler.setProperty(TestElement.TEST_CLASS, testClass);

        sampler.setProperty("comments", this.comments);
        sampler.setProperty("baseUrl", this.baseUrl);
        sampler.setProperty("connectTimeoutMs", this.connectTimeoutMs);
        sampler.setProperty("requestTimeoutMs", this.requestTimeoutMs);
        sampler.setProperty("authorizationType", this.authorizationType);
        sampler.setProperty("bearerToken", this.bearerToken);
        sampler.setProperty("apiKeyHeaderName", this.apiKeyHeaderName);
        sampler.setProperty("apiKeyValue", this.apiKeyValue);
        sampler.setProperty("customHeadersJson", this.customHeadersJson);
        sampler.setProperty("saveResultVariable", this.saveResultVariable);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(String connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public String getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(String requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public String getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getApiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    public void setApiKeyHeaderName(String apiKeyHeaderName) {
        this.apiKeyHeaderName = apiKeyHeaderName;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public String getCustomHeadersJson() {
        return customHeadersJson;
    }

    public void setCustomHeadersJson(String customHeadersJson) {
        this.customHeadersJson = customHeadersJson;
    }

    public String getSaveResultVariable() {
        return saveResultVariable;
    }

    public void setSaveResultVariable(String saveResultVariable) {
        this.saveResultVariable = saveResultVariable;
    }
}
