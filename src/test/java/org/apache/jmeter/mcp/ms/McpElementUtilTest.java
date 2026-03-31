package org.apache.jmeter.mcp.ms;

import io.metersphere.plugin.core.MsTestElement;
import org.apache.jmeter.mcp.runtime.McpSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpElementUtilTest {
    @Test
    void setBaseParamsBuildsScenarioAndPathProperties() {
        TestSampler sampler = new TestSampler();
        MsTestElement scenario = new TestElement();
        scenario.setType("scenario");
        scenario.setResourceId("sc1");
        scenario.setName("Scenario");
        scenario.setIndex("0");

        MsTestElement parentStep = new TestElement();
        parentStep.setType("step");
        parentStep.setName("Parent");
        parentStep.setIndex("1");
        parentStep.setParent(scenario);

        MsTestElement element = new TestElement();
        element.setId("id-1");
        element.setResourceId("res-1");
        element.setName("Child");
        element.setType("sampler");
        element.setIndex("2");
        element.setParent(parentStep);

        McpElementUtil.setBaseParams(sampler, element, null);

        assertEquals("id-1", sampler.getPropertyAsString("MS-ID"));
        assertEquals("id-1_1_2", sampler.getPropertyAsString("MS-RESOURCE-ID"));
        assertTrue(sampler.getPropertyAsString("MS-SCENARIO").contains("sc1_Scenario"));
        assertTrue(sampler.getPropertyAsString("MS-PATH").contains("Parent"));
        assertEquals("org.apache.jmeter.samplers.SampleResult", sampler.getPropertyAsString("RESULT_CLASS"));
    }

    private static final class TestSampler extends McpSamplerBase {
        @Override
        public SampleResult sample(org.apache.jmeter.samplers.Entry e) {
            return null;
        }
    }

    private static final class TestElement extends MsTestElement {
    }
}
