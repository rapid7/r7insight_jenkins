package com.rapid7.jenkins;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class LogDecorator extends LineTransformationOutputStream {

    private final OutputStream wrappedOutputStream;
    private final LogWriter logWriter;

    /**
     * Constructor
     *
     * @param os The OutputStream to decorate
     * @param logWriter  The LogWriter object that writes to Insight Platform
     */
    public LogDecorator(OutputStream os, LogWriter logWriter) {
        this.wrappedOutputStream = os;
        this.logWriter = logWriter;
    }

    /**
     * Called when the end of a line is reached.
     */
    @Override
    protected void eol(byte[] bytes, int length) {
        try {
            processLine(bytes, length);
        } catch (IOException e) {
            // Just print out a trace
            e.printStackTrace();
        } catch (RuntimeException re) {
            // Don't break the build. Just print out a stack trace.
            re.printStackTrace();
        }
    }

    // Should we close this here?
    @Override
    public void close() throws IOException {
        logWriter.close();
        super.close();
        wrappedOutputStream.close();
    }

    private void processLine(byte[] bytes, int length) throws IOException {
        if (length > 0) {
            // Find the end before the new line
            int end = length - 1;
            while (bytes[end] == '\n' || bytes[end] == '\r') {
                end--;
            }
            // TODO Verify that the byte are encoded using the platform default (not UTF8)
            if (end > 0) {
                logWriter.writeLogentry(new String(bytes, 0, end + 1, Charset.forName("ascii")));
                wrappedOutputStream.write(bytes, 0, length);
            }
        }
    }

}
