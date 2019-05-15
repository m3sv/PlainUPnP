package fi.iki.elonen.nanohttpd

/**
 * HTTP Request methods, with the ability to decode a `String` back to its enum value.
 */
enum class Method {
    GET, PUT, POST, DELETE, HEAD;

    companion object {
        @JvmStatic
        fun lookup(method: String): Method? {
            for (m in values()) {
                if (m.toString().equals(method, ignoreCase = true)) {
                    return m
                }
            }
            return null
        }
    }
}
