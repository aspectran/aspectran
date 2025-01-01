/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils.logging.log4j2;

import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * The Class Log4j2LoggerWrapper.
 */
public class Log4j2LoggerWrapper implements Logger {

    private static final Marker MARKER = MarkerManager.getMarker(LoggerFactory.MARKER);

    private final transient org.apache.logging.log4j.Logger internalLogger;

    public Log4j2LoggerWrapper(org.apache.logging.log4j.Logger internalLogger) {
        this.internalLogger = internalLogger;
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
        internalLogger.error(MARKER, s);
    }

    @Override
    public void error(String s, Throwable e) {
        internalLogger.error(MARKER, s, e);
    }

    @Override
    public void debug(String s) {
        internalLogger.debug(MARKER, s);
    }

    @Override
    public void debug(String s, Throwable e) {
        internalLogger.debug(MARKER, s, e);
    }

    @Override
    public void info(String s) {
        internalLogger.info(MARKER, s);
    }

    @Override
    public void trace(String s) {
        internalLogger.trace(MARKER, s);
    }

    @Override
    public void warn(String s) {
        internalLogger.warn(MARKER, s);
    }

    @Override
    public void warn(String s, Throwable e) {
        internalLogger.warn(MARKER, s, e);
    }

}
