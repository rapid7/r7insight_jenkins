package com.rapid7.jenkins;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;


public class LogDecoratorTest {

    /**
     * UTF-8 output character set.
     */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private OutputStream mockOs;
    private LogWriter mockLogWriter;
    private LogDecorator logDecorator;


    /**
     * Creates the mocks used in the tests.
     *
     * @throws IOException          Shouldn't happen
     * @throws UnknownHostException Shouldn't happen
     */
    @Before
    public void initMocks() throws UnknownHostException, IOException {
        mockOs = createMock(OutputStream.class);
        mockLogWriter = createMock(LogWriter.class);
        logDecorator = new LogDecorator(mockOs, mockLogWriter);
    }

    /**
     * Verifies that lines are written to both the wrapped OutputStream and the
     * LogWriter.
     *
     * @throws IOException Shouldn't happen
     */
    @Test
    public void writeLines() throws IOException {
        String[] lines = new String[]{
                "Output line1",
                "Output line2",
                "Output line3"
        };
        for (String line : lines) {
            mockLogWriter.writeLogentry(line);
            mockOs.write(ByteArrayStartsWith.startsWIthBytes((line + "\n").getBytes(UTF8)), eq(0), eq(line.length() + 1));
        }
        replay(mockLogWriter, mockOs);

        for (String line : lines) {
            logDecorator.write((line + "\n").getBytes(UTF8));
        }
        verify(mockLogWriter, mockOs);
    }

    /**
     * Verifies that an error in writing to the LogWriter does not cause
     * an exception to bubble.
     *
     * @throws IOException Shouldn't happen
     */
    @Test
    public void writeError() throws IOException {
        String line = "error line";
        mockLogWriter.writeLogentry(line);
        expectLastCall().andThrow(new RuntimeException("Arrrgh"));
        replay(mockLogWriter);
        logDecorator.write((line + "\n").getBytes(UTF8));
    }

    private static class ByteArrayStartsWith implements IArgumentMatcher {

        private final byte[] expectedBytes;
        ;

        public ByteArrayStartsWith(byte[] expectedBytes) {
            this.expectedBytes = expectedBytes;
        }

        public static byte[] startsWIthBytes(byte[] bytes) {
            EasyMock.reportMatcher(new ByteArrayStartsWith(bytes));
            return null;
        }

        public boolean matches(Object actual) {
            return actual instanceof byte[] && matches((byte[]) actual, expectedBytes);

        }

        public boolean matches(byte[] actual, byte[] expected) {
            boolean matches = true;
            if (actual.length >= expected.length) {
                for (int i = 0; i < expected.length && matches; i++) {
                    matches = actual[i] == expected[i];
                }
            } else {
                matches = false;
            }
            return matches;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("starteWIthBytes()");
        }
    }
}
