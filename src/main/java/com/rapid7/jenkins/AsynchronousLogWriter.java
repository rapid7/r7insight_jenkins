package com.rapid7.jenkins;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class to write logs to Rapid7 InsightOps asynchronously
 */
public class AsynchronousLogWriter implements LogWriter {

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final ExecutorService executor;
    private final LogWriter logWriter;

    /**
     * Constructor.
     *
     * @param logWriter Used to write entries to Rapid7 InsightOps.
     */
    public AsynchronousLogWriter(LogWriter logWriter) {
        this.executor = Executors.newSingleThreadExecutor();
        this.logWriter = logWriter;
    }

    /**
     * Writes the given string to Rapid7 InsightOps asynchronously. It would be
     * possible to take an array of bytes as a parameter but we want to make
     * sure it is UTF8 encoded.
     *
     * @param line The line to write.
     */
    public void writeLogentry(final String line) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    logWriter.writeLogentry(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS)) {
                System.err.println("LogWriter shutdown before finished execution");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logWriter.close();
        }
    }
}
