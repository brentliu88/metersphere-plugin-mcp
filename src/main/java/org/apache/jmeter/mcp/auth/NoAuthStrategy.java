package org.apache.jmeter.mcp.auth;

import java.util.Collections;
import java.util.Map;

public final class NoAuthStrategy implements AuthStrategy {
    @Override
    public Map<String, String> headers() {
        return Collections.emptyMap();
    }
}
