package com.aspectran.demo.apm.log;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

public class LogTailerListener implements TailerListener {

    private final String tailerName;

    private final LogtailEndpoint endpoint;

    public LogTailerListener(String tailerName, LogtailEndpoint endpoint) {
        this.tailerName = tailerName;
        this.endpoint = endpoint;
    }

    @Override
    public void init(Tailer tailer) {
    }

    @Override
    public void fileNotFound() {
    }

    @Override
    public void fileRotated() {
    }

    @Override
    public void handle(String line) {
        endpoint.broadcast(tailerName + ":" + line);
    }

    @Override
    public void handle(Exception e) {
    }

}
