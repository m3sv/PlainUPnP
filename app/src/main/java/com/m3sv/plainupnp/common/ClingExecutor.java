package com.m3sv.plainupnp.common;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.seamless.util.Exceptions;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class ClingExecutor extends ThreadPoolExecutor {

    public ClingExecutor() {
        this(new DefaultUpnpServiceConfiguration.ClingThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy() {
                    // The pool is unbounded but rejections will happen during shutdown
                    @Override
                    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                        // Log and discard
                        super.rejectedExecution(runnable, threadPoolExecutor);
                    }
                }
        );
    }

    public ClingExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedHandler) {
        // This is the same as Executors.newCachedThreadPool
        super(0,
                16,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                threadFactory,
                rejectedHandler
        );
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        if (throwable != null) {
            Throwable cause = Exceptions.unwrap(throwable);
            if (cause instanceof InterruptedException) {
                // Ignore this, might happen when we shutdownNow() the executor. We can't
                // log at this point as the logging system might be stopped already (e.g.
                // if it's a CDI component).
                return;
            }
            // Log only
            Timber.w("Thread terminated " + runnable + " abruptly with exception: " + throwable);
            Timber.w("Root cause: " + cause);
        }
    }
}
