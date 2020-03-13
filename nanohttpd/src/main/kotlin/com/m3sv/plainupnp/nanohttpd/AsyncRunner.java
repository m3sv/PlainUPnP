package com.m3sv.plainupnp.nanohttpd;

/**
 * Pluggable strategy for asynchronously executing requests.
 */
public interface AsyncRunner {
    void exec(Runnable code);
}
