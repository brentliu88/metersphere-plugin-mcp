package org.apache.jmeter.mcp.runtime;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpSamplerBaseTest {
    private TestSampler sampler;

    @BeforeEach
    void setUp() {
        sampler = new TestSampler();
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    @Test
    void startSampleInitializesResultMetadata() {
        SampleResult result = sampler.callStartSample("label");

        assertEquals("label", result.getSampleLabel());
        assertEquals("application/json", result.getContentType());
        assertEquals(SampleResult.TEXT, result.getDataType());
    }

    @Test
    void endSuccessMarksResultSuccessful() {
        SampleResult result = sampler.callStartSample("label");

        sampler.callEndSuccess(result, "ok", "{\"a\":1}");

        assertTrue(result.isSuccessful());
        assertEquals("200", result.getResponseCode());
        assertEquals("ok", result.getResponseMessage());
        assertEquals("{\"a\":1}", result.getResponseDataAsString());
    }

    @Test
    void endFailureMarksResultFailed() {
        SampleResult result = sampler.callStartSample("label");

        sampler.callEndFailure(result, new IllegalStateException("boom"));

        assertFalse(result.isSuccessful());
        assertEquals("500", result.getResponseCode());
        assertEquals("boom", result.getResponseMessage());
        assertTrue(result.getResponseDataAsString().contains("IllegalStateException: boom"));
    }

    @Test
    void variableHelpersPersistAndReadValues() {
        sampler.callSetVar("A", "1");
        sampler.callSaveResultVariable(" B ", "2");

        assertEquals("1", sampler.callGetVar("A"));
        assertEquals("2", sampler.callGetVar("B"));
    }

    @Test
    void saveResultVariableSkipsBlankOrNullValues() {
        sampler.callSaveResultVariable(" ", "x");
        sampler.callSaveResultVariable("X", null);

        assertNull(sampler.callGetVar("X"));
    }

    @Test
    void resolvedPropertyHelpersHandleDefaultsAndTypes() {
        sampler.setProperty("name", "value");
        sampler.setProperty("blank", " ");
        sampler.setProperty("longOk", "42");
        sampler.setProperty("longBad", "x");
        sampler.setProperty("boolTrue", "true");

        assertEquals("value", sampler.callGetResolvedProperty("name"));
        assertEquals("fallback", sampler.callGetResolvedPropertyOrDefault("blank", "fallback"));
        assertEquals(42L, sampler.callGetLongProperty("longOk", 9L));
        assertEquals(9L, sampler.callGetLongProperty("longBad", 9L));
        assertTrue(sampler.callGetBooleanProperty("boolTrue", false));
        assertTrue(sampler.callGetBooleanProperty("missing", true));
    }

    @Test
    void endFailureHandlesNullException() {
        SampleResult result = sampler.callStartSample("label");

        sampler.callEndFailure(result, null);

        assertFalse(result.isSuccessful());
        assertEquals("ERROR", result.getResponseMessage());
        assertNotNull(result.getResponseDataAsString());
    }

    private static final class TestSampler extends McpSamplerBase {
        @Override
        public SampleResult sample(org.apache.jmeter.samplers.Entry e) {
            return null;
        }

        SampleResult callStartSample(String label) { return startSample(label); }
        void callEndSuccess(SampleResult result, String message, String responseJson) { endSuccess(result, message, responseJson); }
        void callEndFailure(SampleResult result, Exception ex) { endFailure(result, ex); }
        void callSetVar(String name, String value) { setVar(name, value); }
        void callSaveResultVariable(String name, String value) { saveResultVariable(name, value); }
        String callGetVar(String name) { return getVar(name); }
        String callGetResolvedProperty(String name) { return getResolvedProperty(name); }
        String callGetResolvedPropertyOrDefault(String name, String defaultValue) { return getResolvedPropertyOrDefault(name, defaultValue); }
        long callGetLongProperty(String name, long defaultValue) { return getLongProperty(name, defaultValue); }
        boolean callGetBooleanProperty(String name, boolean defaultValue) { return getBooleanProperty(name, defaultValue); }
    }
}
