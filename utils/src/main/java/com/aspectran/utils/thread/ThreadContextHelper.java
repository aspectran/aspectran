package com.aspectran.utils.thread;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * <p>Created: 2024-12-30</p>
 */
public abstract class ThreadContextHelper {

    /**
     * Override the thread context ClassLoader with the environment's bean ClassLoader
     * if necessary, i.e. if the bean ClassLoader is not equivalent to the thread
     * context ClassLoader already.
     * @param classLoaderToUse the actual ClassLoader to use for the thread context
     * @return the original thread context ClassLoader, or {@code null} if not overridden
     */
    @Nullable
    public static ClassLoader overrideThreadContextClassLoader(@Nullable ClassLoader classLoaderToUse) {
        if (classLoaderToUse != null) {
            Thread currentThread = Thread.currentThread();
            ClassLoader contextClassLoader = currentThread.getContextClassLoader();
            if (!classLoaderToUse.equals(contextClassLoader)) {
                currentThread.setContextClassLoader(classLoaderToUse);
                return contextClassLoader;
            }
        }
        return null;
    }

    public static void restoreThreadContextClassLoader(@Nullable ClassLoader classLoader) {
        if (classLoader != null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    public static <R> R run(ClassLoader classLoader, @NonNull Action<R> action) {
        ClassLoader old = overrideThreadContextClassLoader(classLoader);
        try {
            return action.execute();
        } finally {
            restoreThreadContextClassLoader(old);
        }

    }

    public interface Action<R> {

        R execute();

    }

}
