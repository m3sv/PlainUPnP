package com.m3sv.plainupnp.nanohttpd

/**
 * HTTP Request methods, with the ability to decode a `String` back to its enum value.
 */
enum class Method {
    GET, PUT, POST, DELETE, HEAD;

    companion object {
        @JvmStatic
        fun lookup(method: String): Method? = values().find { it.toString().equals(method, ignoreCase = true) }
    }
}
