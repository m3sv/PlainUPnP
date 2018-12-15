package fi.iki.elonen.nanohttpd;

/**
 * HTTP Request methods, with the ability to decode a <code>String</code> back to its enum value.
 */
public enum Method {
    GET, PUT, POST, DELETE, HEAD;

    static Method lookup(String method) {
        for (Method m : Method.values()) {
            if (m.toString().equalsIgnoreCase(method)) {
                return m;
            }
        }
        return null;
    }
}
