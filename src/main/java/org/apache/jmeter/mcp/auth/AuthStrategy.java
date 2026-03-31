package org.apache.jmeter.mcp.auth;

import java.util.Map;

public interface AuthStrategy {
    Map<String, String> headers();
}
