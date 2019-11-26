package com.aspectran.demo.apm.log;

import com.aspectran.core.util.lifecycle.AbstractLifeCycle;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import java.io.File;

public class LogTailer extends AbstractLifeCycle {

    /** the log file to tail */
    private final String logFile;

    /** how frequently to check for file changes; defaults to 1 second */
    private long sampleInterval = 1000;

    private TailerListener tailerListener;

    private Tailer tailer;

    public LogTailer(String logFile) {
        this.logFile = logFile;
    }

    public LogTailer(String logFile, long sampleInterval) {
        this.logFile = logFile;
        this.sampleInterval = sampleInterval;
    }

    public void setTailerListener(String tailerName, LogtailEndpoint endpoint) {
        this.tailerListener = new LogTailerListener(tailerName, endpoint);
    }

    protected void doStart() throws Exception {
        if (tailerListener != null) {
            tailer = Tailer.create(new File(logFile), tailerListener, sampleInterval, true);
        }
    }

    protected void doStop() throws Exception {
        if (tailer != null) {
            tailer.stop();
            tailer = null;
            tailerListener = null;
        }
    }

}
