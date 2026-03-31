package org.apache.jmeter.mcp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON: " + json, e);
        }
    }

    public static JsonNode valueToTree(Object value) {
        return MAPPER.valueToTree(value);
    }
}
