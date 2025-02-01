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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * The Class Log4j2ExtendedLoggerWrapper.
 */
public class Log4j2ExtendedLoggerWrapper implements Logger {

    private static final String FQCN = Log4j2Logger.class.getName();

    private final transient ExtendedLogger extendedLogger;

    Log4j2ExtendedLoggerWrapper(ExtendedLogger extendedLogger) {
        this.extendedLogger = extendedLogger;
    }

    @Override
    public boolean isDebugEnabled() {
        return extendedLogger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return extendedLogger.isTraceEnabled();
    }

    @Override
    public void error(String s) {
        extendedLogger.logIfEnabled(FQCN, Level.ERROR, null, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void error(String s, Throwable e) {
        extendedLogger.logIfEnabled(FQCN, Level.ERROR, null, (Message) new SimpleMessage(s), e);
    }

    @Override
    public void debug(String s) {
        extendedLogger.logIfEnabled(FQCN, Level.DEBUG, null, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void debug(String s, Throwable e) {
        extendedLogger.logIfEnabled(FQCN, Level.DEBUG, null, (Message) new SimpleMessage(s), e);
    }

    @Override
    public void info(String s) {
        extendedLogger.logIfEnabled(FQCN, Level.INFO, null, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void trace(String s) {
        extendedLogger.logIfEnabled(FQCN, Level.TRACE, null, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void warn(String s) {
        extendedLogger.logIfEnabled(FQCN, Level.WARN, null, (Message) new SimpleMessage(s), null);
    }

    @Override
    public void warn(String s, Throwable e) {
        extendedLogger.logIfEnabled(FQCN, Level.WARN, null, (Message) new SimpleMessage(s), e);
    }

}
