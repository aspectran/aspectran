/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

/**
 * The Class Log4j2ExtendedLoggerWrapper.
 */
public class Log4j2ExtendedLoggerWrapper implements Logger {

    private static final Marker MARKER = MarkerManager.getMarker(LoggerFactory.MARKER);

    private static final String FQCN = Log4j2Logger.class.getName();

    private final transient ExtendedLoggerWrapper internalLogger;

    public Log4j2ExtendedLoggerWrapper(ExtendedLogger extendedLogger) {
        internalLogger = new ExtendedLoggerWrapper(extendedLogger, extendedLogger.getName(), extendedLogger.getMessageFactory());
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
        internalLogger.logIfEnabled(FQCN, Level.ERROR, MARKER, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void error(String s, Throwable e) {
        internalLogger.logIfEnabled(FQCN, Level.ERROR, MARKER, (Message) new SimpleMessage(s), e);
    }

    @Override
    public void debug(String s) {
        internalLogger.logIfEnabled(FQCN, Level.DEBUG, MARKER, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void debug(String s, Throwable e) {
        internalLogger.logIfEnabled(FQCN, Level.DEBUG, MARKER, (Message) new SimpleMessage(s), e);
    }

    @Override
    public void info(String s) {
        internalLogger.logIfEnabled(FQCN, Level.INFO, MARKER, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void trace(String s) {
        internalLogger.logIfEnabled(FQCN, Level.TRACE, MARKER, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void warn(String s) {
        internalLogger.logIfEnabled(FQCN, Level.WARN, MARKER, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void warn(String s, Throwable e) {
        internalLogger.logIfEnabled(FQCN, Level.WARN, MARKER, (Message) new SimpleMessage(s), e);
    }

}
