package org.apache.jmeter.mcp.runtime;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.nio.charset.StandardCharsets;

public abstract class McpSamplerBase extends AbstractSampler {
    protected SampleResult startSample(String label) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(label);
        result.setDataType(SampleResult.TEXT);
        result.setContentType("application/json");
        result.sampleStart();
        return result;
    }

    protected void endSuccess(SampleResult result, String message, String responseJson) {
        result.setSuccessful(true);
        result.setResponseCode("200");
        result.setResponseMessage(message);
        if (responseJson != null) {
            result.setResponseData(responseJson, StandardCharsets.UTF_8.name());
        }
        result.sampleEnd();
    }

    protected void endFailure(SampleResult result, Exception ex) {
        result.setSuccessful(false);
        result.setResponseCode("500");
        result.setResponseMessage(ex == null ? "ERROR" : ex.getMessage());
        result.setResponseData(stackTraceLike(ex), StandardCharsets.UTF_8.name());
        result.sampleEnd();
    }

    protected void setVar(String name, String value) {
        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        if (vars != null && name != null && value != null) {
            vars.put(name, value);
        }
    }

    protected void saveResultVariable(String varName, String value) {
        if (varName == null || varName.isBlank() || value == null) {
            return;
        }
        setVar(varName.trim(), value);
    }

    protected String getVar(String name) {
        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        return vars == null ? null : vars.get(name);
    }

    protected String getResolvedProperty(String name) {
        return resolveDynamicValue(getPropertyAsString(name));
    }

    protected String getResolvedPropertyOrDefault(String name, String defaultValue) {
        String value = getResolvedProperty(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    protected long getLongProperty(String name, long defaultValue) {
        String value = getResolvedProperty(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected boolean getBooleanProperty(String name, boolean defaultValue) {
        String value = getResolvedProperty(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private String resolveDynamicValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new CompoundVariable(value).execute();
        } catch (Exception ignore) {
            return value;
        }
    }

    private String stackTraceLike(Exception ex) {
        if (ex == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append('\n');
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("    at ").append(element).append('\n');
        }
        return sb.toString();
    }
}
