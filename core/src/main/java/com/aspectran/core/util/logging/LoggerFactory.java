/*
 * Copyright (c) 2008-2022 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.util.logging;

import com.aspectran.core.util.logging.commons.JakartaCommonsLogger;
import com.aspectran.core.util.logging.jdk14.Jdk14Logger;
import com.aspectran.core.util.logging.log4j2.Log4j2Logger;
import com.aspectran.core.util.logging.nologging.DumbLogger;
import com.aspectran.core.util.logging.slf4j.Slf4jLogger;
import com.aspectran.core.util.logging.stdout.StdOutLogger;

import java.lang.reflect.Constructor;

/**
 * The LoggerFactory is a utility class producing Loggers for various logging APIs.
 */
public final class LoggerFactory {

    /**
     * Marker to be used by logging implementations that support markers.
     */
    public static final String MARKER = "ASPECTRAN";

    static final String EXCEPTION_MESSAGE = "Unexpected exception: ";

    private static Constructor<? extends Logger> loggerConstructor;

    static {
        tryImplementation(LoggerFactory::useSlf4jLogging);
        tryImplementation(LoggerFactory::useCommonsLogging);
        tryImplementation(LoggerFactory::useLog4J2Logging);
        tryImplementation(LoggerFactory::useJdkLogging);
        tryImplementation(LoggerFactory::useNoLogging);
    }

    private LoggerFactory() {
        // disable construction
    }

    public static Logger getLogger(Class<?> aClass) {
        return getLogger(aClass.getName());
    }

    public static Logger getLogger(String name) {
        try {
            return loggerConstructor.newInstance(name);
        } catch (Throwable t) {
            throw new LoggerException("Error creating logger for " + name + ".  Cause: " + t, t);
        }
    }

    public static synchronized void useCustomLogging(Class<? extends Logger> clazz) {
        setImplementation(clazz);
    }

    public static synchronized void useSlf4jLogging() {
        setImplementation(Slf4jLogger.class);
    }

    public static synchronized void useCommonsLogging() {
        setImplementation(JakartaCommonsLogger.class);
    }

    public static synchronized void useLog4J2Logging() {
        setImplementation(Log4j2Logger.class);
    }

    public static synchronized void useJdkLogging() {
        setImplementation(Jdk14Logger.class);
    }

    public static synchronized void useStdOutLogging() {
        setImplementation(StdOutLogger.class);
    }

    public static synchronized void useNoLogging() {
        setImplementation(DumbLogger.class);
    }

    private static void tryImplementation(Runnable runnable) {
        if (loggerConstructor == null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private static void setImplementation(Class<? extends Logger> implClass) {
        try {
            Constructor<? extends Logger> candidate = implClass.getConstructor(String.class);
            Logger logger = candidate.newInstance(LoggerFactory.class.getName());
            if (logger.isDebugEnabled()) {
                logger.debug("Logging initialized using '" + implClass + "' adapter");
            }
            loggerConstructor = candidate;
        } catch (Throwable t) {
            throw new LoggerException("Error setting Logger implementation.  Cause: " + t, t);
        }
    }

}
