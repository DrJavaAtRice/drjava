package edu.rice.cs.util;

import java.io.*;

/** StreamRedirectThread is a thread which copies its input to its output and terminates when it completes. */
public class StreamRedirectThread extends Thread {
    /// Input reader
    private final Reader in;

    /// Output writer
    private final Writer out;

    /// Data buffer size
    private static final int BUFFER_SIZE = 2048;

    /**
     * Constructor
     *
     * @param name thread name
     * @param in   stream to copy from
     * @param out  stream to copy to
     */
    public StreamRedirectThread(String name, InputStream in, OutputStream out) {
        super(name);
        this.in = new InputStreamReader(in);
        this.out = new OutputStreamWriter(out);
        setPriority(Thread.MAX_PRIORITY - 1);
    }

    /**
     * Copy.
     */
    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            int count;
            while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
                out.write(cbuf, 0, count);
                out.flush();
            }
            out.flush();
        }
        catch (IOException exc) {
          // ignore
        }
    }
}
