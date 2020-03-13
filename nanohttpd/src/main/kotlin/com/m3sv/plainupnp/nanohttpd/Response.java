package com.m3sv.plainupnp.nanohttpd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.m3sv.plainupnp.nanohttpd.NanoHttpdUtils.MIME_HTML;

/**
 * HTTP response. Return one of these from serve().
 */
public class Response {
    /**
     * HTTP status code after processing, e.g. "200 OK", HTTP_OK
     */
    private Status status;
    /**
     * MIME type of content, e.g. "text/html"
     */
    private String mimeType;
    /**
     * Data of the response, may be null.
     */
    private InputStream data;
    /**
     * Headers for the HTTP response. Use addHeader() to add lines.
     */
    private Map<String, String> header = new HashMap<>();
    /**
     * The request method that spawned this response.
     */
    private Method requestMethod;

    /**
     * Default constructor: response = HTTP_OK, mime = MIME_HTML and your supplied message
     */
    public Response(String msg) {
        this(Status.OK, MIME_HTML, msg);
    }

    /**
     * Basic constructor.
     */
    public Response(Status status, String mimeType, InputStream data) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
    }

    /**
     * Convenience method that makes an InputStream out of given text.
     */
    public Response(Status status, String mimeType, String txt) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = txt != null ? new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8)) : null;
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(String name, String value) {
        header.put(name, value);
    }

    /**
     * Sends given response to the socket.
     */
    public void send(OutputStream outputStream) {
        String mime = mimeType;
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            if (status == null) {
                throw new Error("sendResponse(): Status can't be null.");
            }
            PrintWriter pw = new PrintWriter(outputStream);
            pw.print("HTTP/1.1 " + status.getDescription() + " \r\n");

            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }

            if (header == null || header.get("Date") == null) {
                pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
            }

            if (header != null) {
                for (String key : header.keySet()) {
                    String value = header.get(key);
                    pw.print(key + ": " + value + "\r\n");
                }
            }

            int pending = data != null ? data.available() : -1; // This is to support partial sends, see serveFile()
            if (pending > 0) {
                pw.print("Connection: keep-alive\r\n");
                pw.print("Content-Length: " + pending + "\r\n");
            }

            pw.print("\r\n");
            pw.flush();

            if (requestMethod != Method.HEAD && data != null) {
                int BUFFER_SIZE = 16 * 1024;
                byte[] buff = new byte[BUFFER_SIZE];
                while (pending > 0) {
                    int read = data.read(buff, 0, ((pending > BUFFER_SIZE) ? BUFFER_SIZE : pending));
                    if (read <= 0) {
                        break;
                    }
                    outputStream.write(buff, 0, read);

                    pending -= read;
                }
            }
            outputStream.flush();
            if (data != null)
                NanoHttpdUtils.safeClose(data);
        } catch (IOException ioe) {
            // Couldn't write? No can do.
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public Method getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(Method requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Some HTTP response status codes
     */
    public enum Status {
        OK(200, "OK"), CREATED(201, "Created"), ACCEPTED(202, "Accepted"), NO_CONTENT(204, "No Content"), PARTIAL_CONTENT(206, "Partial Content"), REDIRECT(301,
                "Moved Permanently"), NOT_MODIFIED(304, "Not Modified"), BAD_REQUEST(400, "Bad Request"), UNAUTHORIZED(401,
                "Unauthorized"), FORBIDDEN(403, "Forbidden"), NOT_FOUND(404, "Not Found"), RANGE_NOT_SATISFIABLE(416,
                "Requested Range Not Satisfiable"), INTERNAL_ERROR(500, "Internal Server Error");
        private final int requestStatus;
        private final String description;

        Status(int requestStatus, String description) {
            this.requestStatus = requestStatus;
            this.description = description;
        }

        public int getRequestStatus() {
            return this.requestStatus;
        }

        public String getDescription() {
            return "" + this.requestStatus + " " + description;
        }
    }
}
