/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util.logging.log4j2;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

/**
 * @author Eduardo Macarron
 */
public class Log4j2AbstractLoggerImpl implements Log {

    private static Marker MARKER = MarkerManager.getMarker(LogFactory.MARKER);

    private static final String FQCN = Log4j2Impl.class.getName();

    private ExtendedLoggerWrapper log;

    public Log4j2AbstractLoggerImpl(AbstractLogger abstractLogger) {
        log = new ExtendedLoggerWrapper(abstractLogger, abstractLogger.getName(), abstractLogger.getMessageFactory());
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s) {
        log.logIfEnabled(FQCN, Level.ERROR, MARKER, s);
    }

    @Override
    public void error(String s, Throwable e) {
        log.logIfEnabled(FQCN, Level.ERROR, MARKER, s, e);
    }

    @Override
    public void debug(String s) {
        log.logIfEnabled(FQCN, Level.DEBUG, MARKER, s);
    }

    @Override
    public void debug(String s, Throwable e) {
        log.logIfEnabled(FQCN, Level.DEBUG, MARKER, s, e);
    }

    @Override
    public void info(String s) {
        log.logIfEnabled(FQCN, Level.INFO, MARKER, s);
    }

    @Override
    public void trace(String s) {
        log.logIfEnabled(FQCN, Level.TRACE, MARKER, s);
    }

    @Override
    public void warn(String s) {
        log.logIfEnabled(FQCN, Level.WARN, MARKER, s);
    }

    @Override
    public void warn(String s, Throwable e) {
        log.logIfEnabled(FQCN, Level.WARN, MARKER, s, e);
    }

}
