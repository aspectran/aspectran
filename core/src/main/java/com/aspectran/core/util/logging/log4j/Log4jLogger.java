/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
package com.aspectran.core.util.logging.log4j;

import com.aspectran.core.util.logging.Logger;
import org.apache.log4j.Level;

/**
 * <a href="http://logging.apache.org/log4j/1.2/index.html">Apache Log4J</a> logger.
 */
public class Log4jLogger implements Logger {

    private static final String FQCN = Log4jLogger.class.getName();

    private final transient org.apache.log4j.Logger internalLogger;

    public Log4jLogger(String clazz) {
        internalLogger = org.apache.log4j.Logger.getLogger(clazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return internalLogger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return internalLogger.isTraceEnabled();
    }

    @Override
    public void error(String s) {
        internalLogger.log(FQCN, Level.ERROR, s, null);
    }

    @Override
    public void error(String s, Throwable e) {
        internalLogger.log(FQCN, Level.ERROR, s, e);
    }

    @Override
    public void debug(String s) {
        internalLogger.log(FQCN, Level.DEBUG, s, null);
    }

    @Override
    public void debug(String s, Throwable e) {
        internalLogger.log(FQCN, Level.DEBUG, s, e);
    }

    @Override
    public void info(String s) {
        internalLogger.log(FQCN, Level.INFO, s, null);
    }

    @Override
    public void trace(String s) {
        internalLogger.log(FQCN, Level.TRACE, s, null);
    }

    @Override
    public void warn(String s) {
        internalLogger.log(FQCN, Level.WARN, s, null);
    }

    @Override
    public void warn(String s, Throwable e) {
        internalLogger.log(FQCN, Level.WARN, s, e);
    }

}
