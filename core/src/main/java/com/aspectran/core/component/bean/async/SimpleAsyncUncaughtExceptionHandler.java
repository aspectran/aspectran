package com.aspectran.core.component.bean.async;

import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * A default {@link AsyncUncaughtExceptionHandler} that simply logs the exception.
 */
public class SimpleAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAsyncUncaughtExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, @Nullable Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error("Unexpected exception occurred invoking async method: {}", method, ex);
        }
    }

}
