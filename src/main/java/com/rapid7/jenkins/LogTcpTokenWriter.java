package com.rapid7.jenkins;


import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Writes lines to Rapid7 InsightOps using the Token TCP input
 */
public class LogTcpTokenWriter implements LogWriter {

    /**
     * Rapid7 InsightOps API server address.
     */
    public static final String DATA_ENDPOINT_TEMPLATE = "%s.data.logs.insight.rapid7.com";
    /**
     * Port number for Token logging on Rapid7 InsightOps API server.
     */
    private static final int PORT = 443;
    /**
     * UTF-8 output character set.
     */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final String token;
    private final Socket socket;
    private final OutputStream outputStream;

    /**
     * Constructor
     *
     * @param token The token for the logfile
     * @param endpoint Endpoints to transmit the logs
     * @throws IOException If there was a problem connecting to Rapid7 InsightOps.
     */
    public LogTcpTokenWriter(String token, String endpoint) throws IOException {
        this.token = token;
        socket = SSLSocketFactory.getDefault().createSocket(endpoint, PORT);
        outputStream = socket.getOutputStream();
    }

    /**
     * Write the given line to Rapid7 InsightOps.
     *
     * @param line The line to write.
     * @throws IOException If there was a problem writing the line.
     */
    public void writeLogentry(final String line) throws IOException {
        outputStream.write((token + line + '\n').getBytes(UTF8));
        outputStream.flush();
    }

    /**
     * Closes this writer.
     * TODO implement Closeable?
     */
    public void close() {
        closeStream();
        closeSocket();
    }

    private void closeStream() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
