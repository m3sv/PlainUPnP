package com.m3sv.plainupnp.nanohttpd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public abstract class NanoHTTPD {
    /**
     * Common mime type for dynamic content: plain text
     */
    public static final String MIME_PLAINTEXT = "text/plain";

    /**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    private final String hostname;
    private final int myPort;
    private ServerSocket myServerSocket;
    private Thread myThread;

    /**
     * Constructs an HTTP server on given hostname and port.
     */
    public NanoHTTPD(String hostname, int port) {
        this.hostname = hostname;
        this.myPort = port;
        setTempFileManagerFactory(new DefaultTempFileManagerFactory());
        setAsyncRunner(new ThreadPoolRunner());
    }

    /**
     * Start the server.
     *
     * @throws IOException if the socket is in use.
     */
    public void start() throws IOException {
        closeServerSocket();
        bindServerSocket();
        startListenerThread();
    }

    private void startListenerThread() {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        final Socket finalAccept = myServerSocket.accept();
                        final InputStream inputStream = finalAccept.getInputStream();
                        if (inputStream == null) {
                            NanoHttpdUtils.safeClose(finalAccept);
                        } else {
                            asyncRunner.exec(new Runnable() {
                                @Override
                                public void run() {
                                    OutputStream outputStream = null;
                                    try {
                                        outputStream = finalAccept.getOutputStream();
                                        TempFileManager tempFileManager = tempFileManagerFactory.create();
                                        HTTPSession session = new HTTPSession(NanoHTTPD.this, tempFileManager, inputStream, outputStream);
                                        session.execute();
                                    } catch (IOException e) {
                                        Timber.e(e);
                                    } finally {
                                        if (outputStream != null)
                                            NanoHttpdUtils.safeClose(outputStream);
                                        NanoHttpdUtils.safeClose(inputStream);
                                        NanoHttpdUtils.safeClose(finalAccept);
                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                } while (!myServerSocket.isClosed());
            }
        });

        myThread.setDaemon(true);
        myThread.setName("NanoHttpd Main Listener");
        myThread.start();
    }

    private void bindServerSocket() throws IOException {
        myServerSocket = new ServerSocket();
        SocketAddress socketAddress = (hostname != null) ? new InetSocketAddress(hostname, myPort) : new InetSocketAddress(myPort);
        myServerSocket.bind(socketAddress);
    }

    private void closeServerSocket() {
        try {
            if (myServerSocket != null && !myServerSocket.isClosed()) myServerSocket.close();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    /**
     * Stop the server.
     */
    public void stop() {
        Timber.d("Stop the server");
        try {
            if (myServerSocket != null)
                NanoHttpdUtils.safeClose(myServerSocket);
            myThread.join();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Override this to customize the server.
     * <p/>
     * <p/>
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param uri     Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method  "GET", "POST" etc.
     * @param params  Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @param headers Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    public abstract Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> params,
                                   Map<String, String> files);

    /**
     * Override this to customize the server.
     * <p/>
     * <p/>
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param session The HTTP session
     * @return HTTP response, see class Response for details
     */
    protected Response serve(HTTPSession session) {
        Map<String, String> files = new HashMap<>();

        try {
            session.parseBody(files);
        } catch (IOException ioe) {
            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        }

        String uri = session.getUri();
        Method method = session.getMethod();
        Map<String, String> parms = session.getParms();
        Map<String, String> headers = session.getHeaders();
        return serve(uri, method, headers, parms, files);
    }

    /**
     * Decode percent encoded <code>String</code> values.
     *
     * @param str the percent encoded <code>String</code>
     * @return expanded form of the input, for example "foo%20bar" becomes "foo bar"
     */
    protected String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return decoded;
    }

    /**
     * Pluggable strategy for asynchronously executing requests.
     */
    private AsyncRunner asyncRunner;

    /**
     * Pluggable strategy for asynchronously executing requests.
     *
     * @param asyncRunner new strategy for handling threads.
     */
    public void setAsyncRunner(AsyncRunner asyncRunner) {
        this.asyncRunner = asyncRunner;
    }

    /**
     * Pluggable strategy for asynchronously executing requests.
     */
    public interface AsyncRunner {
        void exec(Runnable code);
    }

    /**
     * Pluggable strategy for creating and cleaning up temporary files.
     */
    private TempFileManagerFactory tempFileManagerFactory;

    /**
     * Pluggable strategy for creating and cleaning up temporary files.
     *
     * @param tempFileManagerFactory new strategy for handling temp files.
     */
    public void setTempFileManagerFactory(TempFileManagerFactory tempFileManagerFactory) {
        this.tempFileManagerFactory = tempFileManagerFactory;
    }

    // ------------------------------------------------------------------------------- //

}
