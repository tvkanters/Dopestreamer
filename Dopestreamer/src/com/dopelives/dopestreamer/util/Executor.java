package com.dopelives.dopestreamer.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles multi-threading operations efficiently by using a thread pool executor.
 */
public class Executor {

    /** The maximum amount of threads in the pool */
    private static final int MAX_NUM_THREADS = 32;

    /** The executor that does the hard work */
    private static final ScheduledThreadPoolExecutor sExecutor;

    static {
        // Prepare the executor
        sExecutor = new ScheduledThreadPoolExecutor(MAX_NUM_THREADS);
        sExecutor.setKeepAliveTime(10, TimeUnit.SECONDS);
        sExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Immediately executes the given task on an available thread.
     *
     * @param task
     *            The task to execute
     */
    public static void execute(final Runnable task) {
        sExecutor.execute(task);
    }

    /**
     * Schedules a repeating task. It may be cancelled by using the returned ScheduleFuture.
     *
     * @param task
     *            The task to execute
     * @param initialDelay
     *            The time to delay first execution in milliseconds
     *
     * @return The ScheduleFuture for further control over the scheduled task
     */
    public static ScheduledFuture<?> schedule(final Runnable task, final long delay) {
        return sExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a repeating task. It may be cancelled by using the returned ScheduleFuture.
     *
     * @param task
     *            The task to execute
     * @param period
     *            The period before the first execution and between successive executions in milliseconds
     *
     * @return The ScheduleFuture for further control over the scheduled task
     */
    public static ScheduledFuture<?> scheduleInterval(final Runnable task, final long delay) {
        return sExecutor.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a repeating task. It may be cancelled by using the returned ScheduleFuture.
     *
     * @param task
     *            The task to execute
     * @param initialDelay
     *            The time to delay first execution in milliseconds
     * @param period
     *            The period between successive executions in milliseconds
     *
     * @return The ScheduleFuture for further control over the scheduled task
     */
    public static ScheduledFuture<?> scheduleInterval(final Runnable task, final long initialDelay, final long delay) {
        return sExecutor.scheduleAtFixedRate(task, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * This is a static-only class.
     */
    private Executor() {}

}
