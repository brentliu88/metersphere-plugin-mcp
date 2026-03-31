package org.apache.jmeter.mcp.client;

public interface ProgressListener {
    void onProgress(String message, Double progress, Double total);
}
