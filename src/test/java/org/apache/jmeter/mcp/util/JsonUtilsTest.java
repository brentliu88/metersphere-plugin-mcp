package org.apache.jmeter.mcp.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonUtilsTest {
    @Test
    void toJsonSerializesValue() {
        String json = JsonUtils.toJson(Map.of("a", 1));

        assertEquals("{\"a\":1}", json);
    }

    @Test
    void readTreeParsesJson() {
        JsonNode node = JsonUtils.readTree("{\"a\":1}");

        assertEquals(1, node.path("a").asInt());
    }

    @Test
    void readTreeThrowsForInvalidJson() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> JsonUtils.readTree("{"));

        assertEquals(true, ex.getMessage().startsWith("Failed to parse JSON:"));
    }

    @Test
    void valueToTreeConvertsObject() {
        JsonNode node = JsonUtils.valueToTree(Map.of("name", "mcp"));

        assertEquals("mcp", node.path("name").asText());
    }
}
