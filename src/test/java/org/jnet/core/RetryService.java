package org.jnet.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

public class RetryService implements Sleep {
    private static final Logger LOGGER = LogManager.getLogger(RetryService.class);

    /**
     * Retry to run a callable until it successes or cancel criterias are met
     *
     * @param callable callable to run
     * @param maxRetry max count of retry runs
     * @param maxTimeoutInMs Defines the timeout how long after starting execution the first time a rerun may occur.
     *            This doesn't say anything about how long the overall execution may take. If the last run takes long,
     *            it won't be canceled when reaching the timeout.
     * @param caughtExceptions Defines on which Exceptions a retry should be triggered, other than that exceptions will
     *            fall through
     * @param methodname Name of the method that is tries to rerun (for logging purposes)
     * @param targetName object the method is called on (for logging purposes)
     * @return result of the callable
     * @throws Propagate Throwable Exception of the last rerun
     */
    public Object runRetrying(CallableWithThrowable<Object> callable, int maxRetry, int maxTimeoutInMs,
            Class<? extends Throwable>[] caughtExceptions, String methodname, String targetName) {
        long start = System.currentTimeMillis();
        Throwable lastException = null;
        int retryCount = 0;
        long timeRunning = System.currentTimeMillis() - start;
        
        // do while timeout is not over but at least one time
        while ((timeRunning < maxTimeoutInMs) || (lastException == null)) {
            LOGGER.debug("Executing method " + methodname + " on " + targetName + ", retrying (" + retryCount + "/"
                    + maxRetry + ")");
            try {
                if (lastException != null) {
                    LOGGER.debug("failed to invoke " + methodname + " on " + targetName + ", retrying (" + retryCount
                            + "/" + maxRetry + "), " + lastException.getClass().getName());
                }
                return callable.call();
            } catch (Throwable th) {
                LOGGER.debug("Executing method " + methodname + " on " + targetName + " failed with exception "
                        + th.getClass().getName());
                lastException = th;
                retryCount++;
                if (!isExceptionOfAnyType(th.getClass(), caughtExceptions) || (retryCount > maxRetry)) {
                    break;
                }
                sleep(50);
            } finally {
                timeRunning = System.currentTimeMillis() - start;
            }
        }

        if (retryCount > 1) {
            LOGGER.error("failed to invoke " + methodname + " on " + targetName + " for " + retryCount + " times ("
                    + timeRunning + " ms). Giving up and throwing exception.");
        }
        throw Throwables.propagate(lastException);
    }

    /**
     * Same as method above, but classname and methodname of the callee are determined from Stacktrace
     *
     * @param callable callable to run
     * @param maxRetry max count of retry runs
     * @param maxTimeoutInMs Defines the timeout how long after starting execution the first time a rerun may occur.
     *            This doesn't say anything about how long the overall execution may take. If the last run takes long,
     *            it won't be canceled when reaching the timeout.
     * @param caughtExceptions Defines on which Exceptions a retry should be triggered, other than that exceptions will
     *            fall through
     * @return result of the callable
     * @throws Propagate Throwable Exception of the last rerun
     */
    public Object runRetrying(CallableWithThrowable<Object> callable, int maxRetry, int maxTimeoutInMs,
            Class<? extends Throwable>[] caughtExceptions) {
        String classname = null;
        String methodname = null;

        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste2 : stes) {
            StackTraceElement ste = ste2;
            if (ste.getClassName().equals(this.getClass().getName())
                    || ste.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            classname = ste.getClassName();
            methodname = ste.getMethodName();
            break;
        }

        return runRetrying(callable, maxRetry, maxTimeoutInMs, caughtExceptions, methodname, classname);
    }

    /**
     * Retry to run a callable until it successes or cancel criterias are met. Criterias are: retry max 1 times,
     * infinite timeout, retry on any {@link Exception}, {@link Error}
     *
     * @param callable callable to run
     * @return result of the callable
     * @throws Propagate Throwable Exception of the last rerun
     */
    @SuppressWarnings("unchecked")
    public Object runRetrying(CallableWithThrowable<Object> callable) {
        return runRetrying(callable, 1, Integer.MAX_VALUE, new Class[] {Exception.class, Error.class});
    }

    /**
     * Retry to run a runnable until it successes or cancel criterias are met. Criterias are: retry max 1 times,
     * infinite timeout, retry on any {@link Exception}, {@link Error}
     *
     * @param runnable runnable to run
     * @throws Propagate Throwable Exception of the last rerun
     */
    public void runRetrying(RunnableWithThrowable runnable) {
        runRetrying(() -> {
            runnable.run();

            // tell the compiler to create a callable, not a runnable
            return 0;
        });
    }

    private static boolean isExceptionOfAnyType(Class<? extends Throwable> th, Class<? extends Throwable>[] thClasses) {
        for (Class<? extends Throwable> thClass : thClasses) {
            if (thClass.isAssignableFrom(th)) {
                return true;
            }
        }

        return false;
    }

    public static interface CallableWithThrowable<T> {
        public T call() throws Throwable;
    }

    public static interface RunnableWithThrowable {
        public void run() throws Throwable;
    }

}
